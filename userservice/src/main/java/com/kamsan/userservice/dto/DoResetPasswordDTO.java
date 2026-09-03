package com.kamsan.userservice.dto;

import java.util.UUID;

public record DoResetPasswordDTO(UUID userPublicId, String token, String password, String confirmPassword) {
}
