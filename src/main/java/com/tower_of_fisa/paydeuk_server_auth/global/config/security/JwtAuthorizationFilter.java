package com.tower_of_fisa.paydeuk_server_auth.global.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final CustomUserDetailsService userDetailsService;
  private final HandlerExceptionResolver resolver;

  public JwtAuthorizationFilter(
      JwtProvider jwtProvider,
      CustomUserDetailsService userDetailsService,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.jwtProvider = jwtProvider;
    this.userDetailsService = userDetailsService;
    this.resolver = resolver;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);

      String username = jwtProvider.extractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        CustomUserDetails userDetails =
            (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    chain.doFilter(request, response);
  }
}
