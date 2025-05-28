package com.tower_of_fisa.paydeuk_server_auth.global.config.security;

import com.tower_of_fisa.paydeuk_server_auth.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_auth.global.config.exception.custom.exception.ForbiddenException403;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final HandlerExceptionResolver resolver;

  public CustomAccessDeniedHandler(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    resolver.resolveException(
        request, response, null, new ForbiddenException403(ErrorDefineCode.AUTHORIZATION_FAIL));
  }
}
