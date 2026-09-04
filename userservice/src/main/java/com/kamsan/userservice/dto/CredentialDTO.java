package com.kamsan.userservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CredentialDTO(
        UUID credentialPublicId,
        String password,
        boolean isExpired,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
