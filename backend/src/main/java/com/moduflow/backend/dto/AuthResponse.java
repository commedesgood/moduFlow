package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Getter
@Schema(description = "Authentication response")
public class AuthResponse {
    @Schema(description = "JWT access token. The frontend stores this in sessionStorage.auth_token.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String accessToken;

    @Schema(description = "Authenticated user information")
    private final UserInfoResponse user;

    @Schema(description = "Token type", example = "Bearer")
    private final String tokenType;

    @Schema(description = "Token expiration in seconds when available", example = "3600")
    private final Long expiresInSeconds;

    @Schema(description = "Authenticated user email", example = "user@example.com")
    private final String email;

    @Schema(description = "Authenticated user role", example = "ADMIN")
    private final String role;

    @Schema(description = "Granted authorities", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
    private final List<String> authorities;

    public AuthResponse(String accessToken, UserInfoResponse user) {
        this(accessToken, user, "Bearer", null, user == null ? null : user.getEmail());
    }

    public AuthResponse(String accessToken,
                        UserInfoResponse user,
                        String tokenType,
                        Long expiresInSeconds,
                        String email) {
        this.accessToken = accessToken;
        this.user = user;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.email = email;
        this.role = user == null || user.getRole() == null ? "USER" : user.getRole();
        this.authorities = authoritiesOf(this.role);
    }

    private List<String> authoritiesOf(String role) {
        if ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
            return List.of("ROLE_ADMIN", "ROLE_USER");
        }
        return List.of("ROLE_USER");
    }
}
