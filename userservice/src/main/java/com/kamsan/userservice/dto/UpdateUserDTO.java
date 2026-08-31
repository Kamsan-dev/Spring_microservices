package com.kamsan.userservice.dto;

import java.util.UUID;

public record UpdateUserDTO(UUID userPublicId,
                            String email,
                            String firstName,
                            String lastName,
                            String bio,
                            String phone,
                            String address) {
}
