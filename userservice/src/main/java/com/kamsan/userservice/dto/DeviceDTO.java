package com.kamsan.userservice.dto;

import java.time.OffsetDateTime;

public record DeviceDTO(
        String machine,
        String client,
        String ipAddress,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
