package com.tower_of_fisa.paydeuk_server_auth.config;

import java.lang.annotation.*;
import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
  long id() default 1L;

  String email() default "test@example.com";
}
