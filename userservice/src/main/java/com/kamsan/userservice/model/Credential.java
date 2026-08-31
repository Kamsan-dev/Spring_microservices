package com.kamsan.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "credentials")
public class Credential {
    @Id
    private Long credentialId;
    private UUID credentialPublicId;
    private Long userId;
    private String password;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
