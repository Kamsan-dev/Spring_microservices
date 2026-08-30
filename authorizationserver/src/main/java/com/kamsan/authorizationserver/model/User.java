package com.kamsan.authorizationserver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long userId;
    private UUID userPublicId;
    private String email;
    private String firstName;
    private String lastName;
    private String memberId;
    private String username;
    private String bio;
    private String imageUrl;
    private boolean isUsingMfa;
    private String qrCodeImageUri;
    private String qrCodeSecret;
    private OffsetDateTime lastLogin;
    private int loginAttempts;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Transient
    private String role;
    @Transient
    private String authorities;
    @Transient
    private String password;

    private boolean isAccountExpired;
    private boolean isAccountLocked;
    private boolean isAccountEnabled;
}

