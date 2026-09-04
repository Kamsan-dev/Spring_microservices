package com.kamsan.userservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageUserDTO(UUID userPublicId,
                          String email,
                          String firstName,
                          String lastName,
                          String memberId,
                          String bio,
                          String imageUrl,
                          String phone,
                          String address,
                          OffsetDateTime createdAt,
                          String role,
                          String authorities) {
}
