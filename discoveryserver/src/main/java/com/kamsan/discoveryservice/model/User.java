package com.kamsan.discoveryservice.model;

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
public class User {
    private Long userId;
    private UUID publicId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String memberId;
    private String username;
    private String bio;
    private String imageUrl;
    private boolean isUsingMFA;
    private String qrCodeImageUri;
    private String qrCodeSecret;
    private String lastLogin;
    private int loginAttempts;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String role;
    private String authorities;
    private boolean isAccountExpired;
    private boolean isAccountLocked;
    private boolean isCredentialsExpired;
    private boolean isAccountEnabled;
}
