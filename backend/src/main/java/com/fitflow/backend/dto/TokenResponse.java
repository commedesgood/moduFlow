package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "레거시 로그인 토큰 응답")
public class TokenResponse {
    @Schema(description = "서비스 JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "만료까지 남은 초", example = "3600")
    private long expiresInSeconds;

    @Schema(description = "로그인 이메일", example = "user@example.com")
    private String email;
}
