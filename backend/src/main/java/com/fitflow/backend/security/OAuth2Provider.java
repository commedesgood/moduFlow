package com.fitflow.backend.security;

import com.fitflow.backend.entity.AuthProvider;

public enum OAuth2Provider {
    GOOGLE(AuthProvider.GOOGLE),
    KAKAO(AuthProvider.KAKAO),
    NAVER(AuthProvider.NAVER);

    private final AuthProvider authProvider;

    OAuth2Provider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public AuthProvider toAuthProvider() {
        return authProvider;
    }
}
