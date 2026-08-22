package com.kamsan.discoveryservice.security.config;

import com.kamsan.discoveryservice.security.key.RSAKeyManager;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@RequiredArgsConstructor
public class JwtConfiguration {
    private final RSAKeyManager rsaKeyManager;

    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKeyManager.getRSAKeyPair().toRSAPublicKey()).build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = rsaKeyManager.getRSAKeyPair();
        JWKSet set = new JWKSet(rsaKey);
        return (j, sc) -> j.select(set);
    }
}
