package com.tower_of_fisa.paydeuk_server_auth.service;

import com.tower_of_fisa.paydeuk_server_auth.domain.entity.User;
import com.tower_of_fisa.paydeuk_server_auth.domain.enums.UserRole;
import com.tower_of_fisa.paydeuk_server_auth.domain.enums.UserStatus;
import com.tower_of_fisa.paydeuk_server_auth.dto.*;
import com.tower_of_fisa.paydeuk_server_auth.global.config.exception.custom.exception.AlreadyExistElementException409;
import com.tower_of_fisa.paydeuk_server_auth.global.config.security.JwtProvider;
import com.tower_of_fisa.paydeuk_server_auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .name("홍길동")
                .username("testUser")
                .password("encodedPassword")
                .phone("01012345678")
                .email("test@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .personalAuthKey("authKey")
                .build();
    }

    @Test
    @DisplayName("AUTH_01: 아이디 찾기 성공")
    void findId_success() {
        given(userRepository.findByPersonalAuthKey("authKey")).willReturn(Optional.of(testUser));

        FindIdResponse result = authService.findId("authKey");

        assertThat(result.getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("AUTH_02: 비밀번호 찾기 성공")
    void findPassword_success() {
        FindPasswordRequest request = new FindPasswordRequest("홍길동", "testUser");
        given(userRepository.findByUsername("testUser")).willReturn(Optional.of(testUser));

        FindPasswordResponse response = authService.findPassword(request);

        assertThat(response.getPersonalAuthKey()).isEqualTo("authKey");
    }

    @Test
    @DisplayName("AUTH_03: 비밀번호 재설정 성공")
    void resetPassword_success() {
        ResetPasswordRequest request = new ResetPasswordRequest("newPassword123!");
        given(userRepository.findByUsername("testUser")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedNewPassword");

        authService.resetPassword("testUser", request);

        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("AUTH_04: 회원가입 검증 - 중복 예외")
    void signupVerify_duplicate() {
        SignupVerifyRequest dummyRequest = new SignupVerifyRequest(
                "홍길동",
                "2000.05.12",
                "010-1234-5678",
                "testUser",
                "hong@example.com",
                "Aa1234!@",
                "uheeR/P2ECGn+AaGPqAe1LB5swI9k/TnDK98Syo7djJerBROsv0M8+OnqpkR2cgZDRMQJFG42dSIk5f5J8IV/w=="
        );
        given(userRepository.findByUsername("testUser")).willReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.signupVerify(dummyRequest))
                .isInstanceOf(AlreadyExistElementException409.class);
    }

    @Test
    @DisplayName("AUTH_05: 회원가입 성공")
    void registerUser_success() {
        SignupRequest request = SignupRequest.builder()
                .name("홍길동")
                .username("newUser")
                .password("pass1234!")
                .email("new@example.com")
                .phone("01099998888")
                .birthdate("19910101")
                .personalAuthKey("newAuthKey")
                .paymentPinCode("1234")
                .build();

        given(userRepository.findByUsername("newUser")).willReturn(Optional.empty());
        given(passwordEncoder.encode(any())).willReturn("encoded");

        authService.registerUser(request);

        then(userRepository).should().save(any(User.class));
    }
}
