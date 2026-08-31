package com.kamsan.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "password_tokens")
public class PasswordToken {
    @Id
    private Long passwordTokenId;
    private Long userId;
    private String token;
    private boolean isExpired;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
