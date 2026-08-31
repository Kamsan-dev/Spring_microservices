package com.kamsan.userservice.repository;

public interface UserSecurityProjection {

    String getPassword();

    String getRole();

    String getAuthorities();

    Boolean getCredentialsExpired();
}
