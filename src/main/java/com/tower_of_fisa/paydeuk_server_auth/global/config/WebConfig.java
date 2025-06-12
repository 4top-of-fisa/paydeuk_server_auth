package com.tower_of_fisa.paydeuk_server_auth.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    corsRegistry
        .addMapping("/**")
        .allowedOrigins(
            "https://paydeuk.com", "https://www.paydeuk.com" // 추가된 도메인
            )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "Access-Control-Allow-Origin")
        .allowCredentials(true); // 쿠키를 포함한 요청 허용
  }
}
