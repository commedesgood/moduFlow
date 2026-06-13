package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
@Schema(description = "Login request")
public class AuthLoginRequest {

    @JsonAlias({"id", "username"})
    @Email(message = "email must be valid")
    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @JsonAlias({"pw", "pwd"})
    @Schema(description = "Password", example = "password123", minLength = 1)
    private String password;

    @JsonAlias({"userId", "user_id", "androidId", "android_id", "deviceId", "device_id"})
    @Schema(description = "Android device identifier for Beacon login", example = "a1b2c3d4e5f6g7h8")
    private String userId;

    @AssertTrue(message = "email/password is required")
    public boolean isValidLoginPayload() {
        boolean hasEmail = hasText(email);
        boolean hasPassword = hasText(password);
        return hasEmail && hasPassword;
    }

    public boolean hasDeviceId() {
        return hasText(userId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
