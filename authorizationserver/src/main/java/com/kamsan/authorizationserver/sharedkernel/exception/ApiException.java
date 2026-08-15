package com.kamsan.authorizationserver.sharedkernel.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
