package com.kamsan.gateway.sharedkernel.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
