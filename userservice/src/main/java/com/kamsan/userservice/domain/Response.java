package com.kamsan.userservice.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record Response(OffsetDateTime time,
                       int code,
                       String path,
                       HttpStatus status,
                       String message,
                       String exception,
                       Map<?, ?> data) {
}
