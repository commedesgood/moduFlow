package com.fitflow.backend.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public CustomException(String message) {
        this(HttpStatus.BAD_REQUEST, "INTERNAL_ERROR", message);
    }

    public CustomException(HttpStatus status, String message) {
        this(status, status == HttpStatus.UNAUTHORIZED ? "AUTH_REQUIRED" : "INTERNAL_ERROR", message);
    }

    public CustomException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
