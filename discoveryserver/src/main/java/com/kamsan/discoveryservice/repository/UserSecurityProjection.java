package com.kamsan.discoveryservice.repository;

public interface UserSecurityProjection {

    String getPassword();

    String getRole();

    String getAuthorities();

    Boolean getCredentialsExpired();
}
