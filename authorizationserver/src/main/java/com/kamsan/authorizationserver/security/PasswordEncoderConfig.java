package com.kamsan.authorizationserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderConfig {
    public static final int STRENGTH = 12;

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder(STRENGTH);
    }
}
