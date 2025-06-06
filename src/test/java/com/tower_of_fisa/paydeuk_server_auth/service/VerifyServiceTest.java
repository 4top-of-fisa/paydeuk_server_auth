package com.tower_of_fisa.paydeuk_server_auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import com.tower_of_fisa.paydeuk_server_auth.dto.VerificationResponse;
import com.tower_of_fisa.paydeuk_server_auth.repository.UserRepository;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class VerifyServiceTest {

  @InjectMocks private VerifyService verifyService;

  @Mock private UserRepository userRepository;

  @Mock private RestTemplate restTemplate;

  @BeforeEach
  void setup() {
    verifyService = new VerifyService(userRepository, restTemplate);
    ReflectionTestUtils.setField(verifyService, "apiKey", "test_api_key");
    ReflectionTestUtils.setField(verifyService, "apiSecret", "test_api_secret");
  }

  @Test
  @DisplayName("본인인증 조회 성공")
  void verifyIdentity_success() {
    // given
    String impUid = "imp_123456";
    String token = "test-access-token";
    String personalAuthKey = "unique_key";

    Map<String, Object> tokenData = new HashMap<>();
    tokenData.put("access_token", token);
    Map<String, Object> tokenResponse = Map.of("response", tokenData);

    Map<String, Object> certData =
        Map.of(
            "certified", true,
            "birthday", "19900101",
            "name", "홍길동",
            "phone", "01012345678",
            "unique_key", personalAuthKey);
    Map<String, Object> certResponse = Map.of("response", certData);

    // mock token 요청
    given(
            restTemplate.exchange(
                eq("https://api.iamport.kr/users/getToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
        .willReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

    // mock 본인인증 정보 요청
    given(
            restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
        .willReturn(new ResponseEntity<>(certResponse, HttpStatus.OK));

    given(userRepository.findByPersonalAuthKey(personalAuthKey)).willReturn(Optional.empty());

    // when
    VerificationResponse result = verifyService.verifyIdentity(impUid);

    // then
    assertThat(result.getCertified()).isTrue();
    assertThat(result.getName()).isEqualTo("홍길동");
    assertThat(result.getDuplicate()).isFalse();
  }
}
