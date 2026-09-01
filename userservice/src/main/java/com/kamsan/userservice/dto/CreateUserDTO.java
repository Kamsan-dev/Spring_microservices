package com.kamsan.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
        @NotBlank
        @Email
        String email,
        String password,
        String firstName,
        String lastName,
        String username) {
}
