package com.kamsan.authorizationserver.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID publicId;
    private boolean isUsingMFA;
    private String email;
    private String qrCodeSecret;
}
