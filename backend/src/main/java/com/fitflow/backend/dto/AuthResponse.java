package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "인증 성공 응답")
public class AuthResponse {
    @Schema(description = "서비스 JWT access token. 프론트는 sessionStorage.auth_token에 저장합니다.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "로그인 사용자 정보")
    private UserInfoResponse user;
}
