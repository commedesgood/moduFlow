package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "프론트엔드, Beacon, OAuth, CORS 연동 설정 요약")
public record IntegrationSettingsResponse(
        @Schema(description = "Swagger/OpenAPI 서버 기준 URL", example = "http://3.39.194.42:8080")
        String serverBaseUrl,

        FrontendContract frontend,
        AuthContract auth,
        BeaconContract beacon,
        CorsContract cors,
        List<String> publicEndpoints,
        List<RuntimeProperty> runtimeProperties
) {
    @Schema(description = "프론트엔드 연동 계약")
    public record FrontendContract(
            @Schema(description = "OAuth 성공/실패 redirect 대상 프론트 base URL", example = "http://localhost:5173")
            String baseUrl,
            @Schema(description = "OAuth 성공 callback path", example = "/oauth/callback")
            String oauthSuccessPath,
            @Schema(description = "OAuth 실패 callback path", example = "/oauth/callback")
            String oauthFailurePath,
            @Schema(description = "OAuth 성공 redirect 예시", example = "http://localhost:5173/oauth/callback?accessToken={JWT}")
            String oauthSuccessRedirect,
            @Schema(description = "OAuth 실패 redirect 예시", example = "http://localhost:5173/oauth/callback?error={message}")
            String oauthFailureRedirect,
            @Schema(description = "프론트 소셜 로그인 버튼이 이동해야 하는 백엔드 URL")
            List<String> socialLoginStartUrls
    ) {
    }

    @Schema(description = "인증 연동 계약")
    public record AuthContract(
            String signupPath,
            String loginPath,
            String mePath,
            @Schema(description = "프론트 토큰 저장 위치", example = "sessionStorage.auth_token")
            String tokenStorageKey,
            @Schema(description = "인증 API 호출 헤더 형식", example = "Authorization: Bearer {accessToken}")
            String authorizationHeader
    ) {
    }

    @Schema(description = "Beacon 위치 연동 계약")
    public record BeaconContract(
            String updateLocationPath,
            String currentLocationPath,
            @Schema(description = "ANDROID_ID를 전달할 때 허용되는 JSON 필드명")
            List<String> userIdFields,
            @Schema(description = "비콘 구역 코드를 전달할 때 허용되는 JSON 필드명")
            List<String> zoneIdFields,
            List<BeaconZone> zoneMappings,
            @Schema(description = "Beacon 위치 API는 JWT 없이 호출 가능한지 여부")
            boolean publicAccess
    ) {
    }

    @Schema(description = "Beacon zoneId와 표시 이름")
    public record BeaconZone(
            Integer zoneId,
            String zoneName
    ) {
    }

    @Schema(description = "CORS 연동 계약")
    public record CorsContract(
            List<String> allowedOriginPatterns,
            boolean credentialsAllowed,
            List<String> exposedHeaders
    ) {
    }

    @Schema(description = "배포/실행 환경에서 맞춰야 하는 설정 키")
    public record RuntimeProperty(
            String env,
            String property,
            boolean secret,
            String note
    ) {
    }
}
