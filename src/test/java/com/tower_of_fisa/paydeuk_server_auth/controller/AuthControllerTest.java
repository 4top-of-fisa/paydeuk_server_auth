package com.tower_of_fisa.paydeuk_server_auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tower_of_fisa.paydeuk_server_auth.config.WithCustomMockUser;
import com.tower_of_fisa.paydeuk_server_auth.dto.*;
import com.tower_of_fisa.paydeuk_server_auth.global.config.security.CustomUserDetailsService;
import com.tower_of_fisa.paydeuk_server_auth.global.config.security.JwtProvider;
import com.tower_of_fisa.paydeuk_server_auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@MockBean(JpaMetamodelMappingContext.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  @MockBean private JwtProvider jwtProvider;

  @Test
  @DisplayName("AUTH_01: 회원가입 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void signup_success() throws Exception {
    SignupRequest dummySignupRequest =
        new SignupRequest(
            "홍길동",
            "2000.05.12",
            "010-1234-5678",
            "hong123",
            "hong@example.com",
            "Aa1234!@",
            "uheeR/P2ECGn+exampleKey==",
            "191559");
    BDDMockito.willDoNothing().given(authService).registerUser(any());

    mockMvc
        .perform(
            post("/api/signup")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dummySignupRequest)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH_01-1: 회원가입 인증 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void signupVerify_success() throws Exception {
    SignupVerifyRequest dummyVerifyRequest =
        new SignupVerifyRequest(
            "홍길동",
            "2000.05.12",
            "010-1234-5678",
            "hong123",
            "hong@example.com",
            "Aa1234!@",
            "uheeR/P2ECGn+exampleKey==");
    BDDMockito.willDoNothing().given(authService).signupVerify(any());

    mockMvc
        .perform(
            post("/api/signup/verify")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dummyVerifyRequest)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH_02: 로그인 문서용")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void loginDocOnly() throws Exception {
    SigninRequest request = new SigninRequest("user", "pass");

    mockMvc
        .perform(
            post("/api/signin")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk()); // 실제 서비스 로직 없음
  }

  @Test
  @DisplayName("AUTH_03: 토큰 재발급 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void refreshToken_success() throws Exception {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("accessToken", "new-access-token");

    BDDMockito.given(authService.refreshAccessToken(eq("refresh-token"), any()))
        .willReturn(tokenMap);

    mockMvc
        .perform(
            post("/api/refresh").with(csrf()).cookie(new Cookie("refreshToken", "refresh-token")))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH_04: 로그아웃 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void logout_success() throws Exception {
    BDDMockito.willDoNothing().given(authService).logout(eq("access-token"), any());

    mockMvc
        .perform(post("/api/logout").with(csrf()).header("Authorization", "Bearer access-token"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH_04: 아이디 찾기 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void findId_success() throws Exception {
    FindIdRequest request = new FindIdRequest("auth-key");
    FindIdResponse response = new FindIdResponse("testUser");
    BDDMockito.given(authService.findId("auth-key")).willReturn(response);

    mockMvc
        .perform(
            post("/api/find-id")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH_05: 비밀번호 찾기 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void findPassword_success() throws Exception {
    FindPasswordRequest request = new FindPasswordRequest("홍길동", "user123");
    FindPasswordResponse response = new FindPasswordResponse("auth-key");
    BDDMockito.given(authService.findPassword(any())).willReturn(response);

    mockMvc
        .perform(
            post("/api/find-password")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH_06: 비밀번호 재설정 성공")
  @WithCustomMockUser(id = 1L, email = "test@example.com")
  void resetPassword_success() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest("New1234!@");
    BDDMockito.willDoNothing().given(authService).resetPassword(eq("user123"), any());

    mockMvc
        .perform(
            post("/api/reset-password")
                .with(csrf())
                .param("username", "user123")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }
}
