package com.kamsan.userservice.dto;

public record CreateUserDTO(
        String email,
        String password,
        String firstName,
        String lastName,
        String username) {
}
