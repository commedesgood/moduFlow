package com.fitflow.backend.controller;

import com.fitflow.backend.dto.IntegrationSettingsResponse;
import com.fitflow.backend.security.AppCorsProperties;
import com.fitflow.backend.security.AppFrontendProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/integration-settings")
@Tag(name = "연계 설정", description = "프론트엔드, Beacon, OAuth, CORS 작업자가 함께 보는 연동 계약")
public class IntegrationSettingsController {

    private final AppFrontendProperties frontendProperties;
    private final AppCorsProperties corsProperties;
    private final String serverBaseUrl;

    public IntegrationSettingsController(AppFrontendProperties frontendProperties,
                                         AppCorsProperties corsProperties,
                                         @Value("${app.server.base-url:}") String serverBaseUrl) {
        this.frontendProperties = frontendProperties;
        this.corsProperties = corsProperties;
        this.serverBaseUrl = serverBaseUrl;
    }

    @GetMapping
    @Operation(
            operationId = "getIntegrationSettings",
            summary = "연계 설정 요약 조회",
            description = """
                    프론트엔드, Beacon, OAuth, CORS 연동 시 맞춰야 하는 공개 계약만 반환합니다.
                    JWT secret, OAuth client secret, DB password 같은 민감값은 노출하지 않고 설정 키 이름만 안내합니다.
                    """
    )
    public IntegrationSettingsResponse get() {
        String frontendBaseUrl = frontendProperties.getBaseUrl();
        String successPath = frontendProperties.getOauthSuccessPath();
        String failurePath = frontendProperties.getOauthFailurePath();

        return new IntegrationSettingsResponse(
                blankToNull(serverBaseUrl),
                frontend(frontendBaseUrl, successPath, failurePath),
                auth(),
                beacon(),
                cors(),
                publicEndpoints(),
                runtimeProperties()
        );
    }

    private IntegrationSettingsResponse.FrontendContract frontend(String baseUrl,
                                                                  String successPath,
                                                                  String failurePath) {
        return new IntegrationSettingsResponse.FrontendContract(
                baseUrl,
                successPath,
                failurePath,
                callbackUrl(baseUrl, successPath, "?accessToken={JWT}"),
                callbackUrl(baseUrl, failurePath, "?error={message}"),
                List.of(
                        "GET /oauth2/authorization/google",
                        "GET /oauth2/authorization/kakao",
                        "GET /oauth2/authorization/naver"
                )
        );
    }

    private IntegrationSettingsResponse.AuthContract auth() {
        return new IntegrationSettingsResponse.AuthContract(
                "POST /api/v1/auth/signup",
                "POST /api/v1/auth/login",
                "GET /api/v1/me",
                "sessionStorage.auth_token",
                "Authorization: Bearer {accessToken}"
        );
    }

    private IntegrationSettingsResponse.BeaconContract beacon() {
        return new IntegrationSettingsResponse.BeaconContract(
                "POST /api/update-location",
                "GET /api/current-location/{userId}",
                List.of("userId", "user_id", "androidId", "android_id", "deviceId", "device_id"),
                List.of("zoneId", "zone_id", "beaconId", "beacon_id", "minor", "beaconMinor", "zoneCode", "zone_code"),
                List.of(
                        new IntegrationSettingsResponse.BeaconZone(53626, "비콘1"),
                        new IntegrationSettingsResponse.BeaconZone(53630, "비콘2"),
                        new IntegrationSettingsResponse.BeaconZone(56376, "비콘3"),
                        new IntegrationSettingsResponse.BeaconZone(0, "이탈")
                ),
                true
        );
    }

    private IntegrationSettingsResponse.CorsContract cors() {
        return new IntegrationSettingsResponse.CorsContract(
                corsProperties.getAllowedOriginPatterns(),
                true,
                List.of("Authorization")
        );
    }

    private List<String> publicEndpoints() {
        return List.of(
                "GET /ping",
                "GET /api/v1/integration-settings",
                "POST /api/v1/auth/signup",
                "POST /api/v1/auth/login",
                "POST /api/update-location",
                "GET /api/current-location/{userId}",
                "GET /oauth2/authorization/{provider}",
                "GET /login/oauth2/code/{provider}"
        );
    }

    private List<IntegrationSettingsResponse.RuntimeProperty> runtimeProperties() {
        return List.of(
                property("FRONTEND_BASE_URL", "app.frontend.base-url", false, "OAuth redirect와 CORS origin 계산에 사용합니다."),
                property("FRONTEND_OAUTH_SUCCESS_PATH", "app.frontend.oauth-success-path", false, "기본값은 /oauth/callback 입니다."),
                property("FRONTEND_OAUTH_FAILURE_PATH", "app.frontend.oauth-failure-path", false, "기본값은 /oauth/callback 입니다."),
                property("JWT_SECRET", "security.jwt.secret", true, "운영 환경에서는 32바이트 이상의 랜덤 문자열이 필요합니다."),
                property("JWT_EXPIRATION", "security.jwt.expiration", false, "기본값은 1h 입니다."),
                property("DB_HOST / DB_NAME / DB_USERNAME / DB_PASSWORD", "spring.datasource.*", true, "운영 DB 연결 설정입니다."),
                property("GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET", "spring.security.oauth2.client.registration.google.*", true, "Google OAuth 사용 시 필요합니다."),
                property("KAKAO_CLIENT_ID / KAKAO_CLIENT_SECRET", "spring.security.oauth2.client.registration.kakao.*", true, "Kakao OAuth 사용 시 필요합니다."),
                property("NAVER_CLIENT_ID / NAVER_CLIENT_SECRET", "spring.security.oauth2.client.registration.naver.*", true, "Naver OAuth 사용 시 필요합니다.")
        );
    }

    private IntegrationSettingsResponse.RuntimeProperty property(String env,
                                                                 String property,
                                                                 boolean secret,
                                                                 String note) {
        return new IntegrationSettingsResponse.RuntimeProperty(env, property, secret, note);
    }

    private String callbackUrl(String baseUrl, String path, String query) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }

        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        String normalizedPath = path == null || path.isBlank()
                ? ""
                : path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath + query;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
