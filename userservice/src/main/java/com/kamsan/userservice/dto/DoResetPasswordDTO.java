package com.kamsan.userservice.dto;

public record DoResetPasswordDTO(String token, String password, String confirmPassword) {
}
