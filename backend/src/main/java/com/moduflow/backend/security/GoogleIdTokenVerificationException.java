package com.moduflow.backend.security;

public class GoogleIdTokenVerificationException extends RuntimeException {

    public GoogleIdTokenVerificationException(String message) {
        super(message);
    }

    public GoogleIdTokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
