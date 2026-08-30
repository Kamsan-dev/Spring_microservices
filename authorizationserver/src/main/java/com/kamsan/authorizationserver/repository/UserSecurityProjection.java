package com.kamsan.authorizationserver.repository;

public interface UserSecurityProjection {

    String getPassword();

    String getRole();

    String getAuthorities();

    Boolean getCredentialsExpired();
}
