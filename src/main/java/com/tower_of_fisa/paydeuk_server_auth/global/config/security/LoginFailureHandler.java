package com.tower_of_fisa.paydeuk_server_auth.global.config.security;

import com.tower_of_fisa.paydeuk_server_auth.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_auth.global.config.exception.custom.exception.AuthCredientialException401;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final HandlerExceptionResolver resolver;

  public LoginFailureHandler(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {

    String username = request.getParameter("username");
    String ip = request.getRemoteAddr();

    log.warn("[로그인 실패] username={}, ip={}, reason={}", username, ip, exception.getMessage());

    resolver.resolveException(
        request,
        response,
        null,
        new AuthCredientialException401(ErrorDefineCode.AUTHENTICATE_FAIL));
  }
}
