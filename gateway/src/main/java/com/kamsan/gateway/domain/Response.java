package com.kamsan.gateway.domain;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;

public record Response(OffsetDateTime time,
                       int code,
                       String path,
                       HttpStatus status,
                       String message,
                       String exception,
                       Map<?, ?> data) {
}
