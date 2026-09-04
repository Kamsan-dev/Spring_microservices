package com.kamsan.userservice.dto;

import java.util.UUID;

public record TicketUserDTO(UUID userPublicId,
                            String email,
                            String firstName,
                            String lastName,
                            String imageUrl) {
}
