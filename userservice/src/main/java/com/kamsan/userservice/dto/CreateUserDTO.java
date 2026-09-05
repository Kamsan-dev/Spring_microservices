package com.kamsan.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record CreateUserDTO(
        @Email(message = "Invalid email address")
        String email,
        @NotEmpty(message = "Field cannot be empty or null")
        String password,
        @NotEmpty(message = "Field cannot be empty or null")
        String firstName,
        @NotEmpty(message = "Field cannot be empty or null")
        String lastName,
        String bio,
        String phone) {
}
