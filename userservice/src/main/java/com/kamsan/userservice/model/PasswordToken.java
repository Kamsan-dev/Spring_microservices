package com.kamsan.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "password_tokens")
public class PasswordToken {
    @Id
    private Long passwordTokenId;
    private Long userId;
    private String token;
    @Transient
    private boolean isExpired;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
