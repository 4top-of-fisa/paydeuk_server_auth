package com.tower_of_fisa.paydeuk_server_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tower_of_fisa.paydeuk_server_auth.config.WithCustomMockUser;
import com.tower_of_fisa.paydeuk_server_auth.dto.VerificationResponse;
import com.tower_of_fisa.paydeuk_server_auth.global.config.security.CustomUserDetailsService;
import com.tower_of_fisa.paydeuk_server_auth.global.config.security.JwtProvider;
import com.tower_of_fisa.paydeuk_server_auth.service.AuthService;
import com.tower_of_fisa.paydeuk_server_auth.service.VerifyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VerifyController.class)
@MockBean(JpaMetamodelMappingContext.class)
class VerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private VerifyService verifyService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtProvider jwtProvider;


    @Test
    @DisplayName("AUTH_07: 본인인증 결과 조회 성공")
    @WithCustomMockUser(id = 1L, email = "test@example.com")
    void verifyIdentity_success() throws Exception {
        VerificationResponse dummyResponse = new VerificationResponse(
                true,
                "19900101",
                "홍길동",
                "01012345678",
                "uheeR/P2ECGn+AaGPqAe1LB5swI9k/TnDK98Syo7djJerBROsv0M8+OnqpkR2cgZDRMQJFG42dSIk5f5J8IV/w==",
                false
        );

        BDDMockito.given(verifyService.verifyIdentity("imp_1234567890")).willReturn(dummyResponse);

        mockMvc.perform(get("/api/verification")
                        .param("imp_uid", "imp_1234567890"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
