package com.moduflow.backend.security;

public record GoogleIdTokenClaims(String subject, String email, String name) {
}
