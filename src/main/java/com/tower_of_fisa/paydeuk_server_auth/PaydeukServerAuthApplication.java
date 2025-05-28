package com.tower_of_fisa.paydeuk_server_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PaydeukServerAuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaydeukServerAuthApplication.class, args);
  }
}
