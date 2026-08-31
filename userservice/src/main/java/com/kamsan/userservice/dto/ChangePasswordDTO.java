package com.kamsan.userservice.dto;

import java.util.UUID;

public record ChangePasswordDTO(UUID userPublicId, String currentPassword, String newPassword,
                                String confirmNewPassword) {
}
