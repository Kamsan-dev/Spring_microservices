package com.kamsan.userservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CredentialDTO(
        UUID credentialPublicId,
        String password,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
