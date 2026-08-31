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
@Table(name = "account_tokens")
public class AccountToken {
    @Id
    private Long accountTokenId;
    private Long userId;
    private String token;
    private boolean isExpired;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
