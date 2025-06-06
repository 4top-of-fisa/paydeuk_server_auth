package com.tower_of_fisa.paydeuk_server_auth.global.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final CustomUserDetailsService userDetailsService;
  private final JwtProvider jwtProvider;
  private final LoginSuccessHandler successHandler;
  private final LoginFailureHandler failureHandler;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;

  /*
  운영상에서는 문제가 없지만 Spring이 SecurityConfig에 주입할 HandlerExceptionResolver 빈을 찾지 못하고 두 개의 후보를 발견해서 충돌하는 상황
  따라서 @Qualifier를 통해 지정이 가능 그러나 BootRun으로 동작시 자동으로 지정을 해주는 것으로 보아 운영상에는 문제가 없을 것 같음
  혹시 실행할 때 에러가 발생한다면 다음의 주석을 제거하고 실행하면 동작 가능
  */
  //  private final @Qualifier("handlerExceptionResolver") HandlerExceptionResolver
  // handlerExceptionResolver;

  private final HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
    builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    return builder.build();
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

    // 로그인 필터 설정
    LoginFilter loginFilter =
        new LoginFilter(new ObjectMapper(), authenticationManager, handlerExceptionResolver);
    loginFilter.setAuthenticationSuccessHandler(successHandler);
    loginFilter.setAuthenticationFailureHandler(failureHandler);
    loginFilter.setFilterProcessesUrl("/api/signin");

    http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            new JwtAuthorizationFilter(jwtProvider, userDetailsService, handlerExceptionResolver),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
