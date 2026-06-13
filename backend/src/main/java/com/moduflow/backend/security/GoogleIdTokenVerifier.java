package com.moduflow.backend.security;

public interface GoogleIdTokenVerifier {
    GoogleIdTokenClaims verify(String idToken);
}
