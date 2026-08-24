package com.kamsan.gateway.security.token;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@Component
public class JwtConverter implements Converter<Jwt, JwtAuthenticationToken> {
    private static final String AUTHORITY_KEY = "authorities";

    @Override
    public JwtAuthenticationToken convert(Jwt jwtSource) {
        var claims = (String) jwtSource.getClaims().get(AUTHORITY_KEY);
        List<GrantedAuthority> grantedAuthorities = commaSeparatedStringToAuthorityList(claims);
        return new JwtAuthenticationToken(jwtSource, grantedAuthorities, Objects.requireNonNull(jwtSource.getSubject()));
    }
}
