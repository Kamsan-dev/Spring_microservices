package com.kamsan.userservice.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ApiResponse(
        int code,
        String message,
        Map<String, ?> data) {
}
