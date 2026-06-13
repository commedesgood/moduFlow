package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Android Google ID token login request")
public class GoogleIdTokenLoginRequest {

    @JsonAlias({"id_token", "googleIdToken", "credential", "token"})
    @NotBlank(message = "idToken is required")
    @Schema(description = "Google ID token returned by the Android Google sign-in flow")
    private String idToken;

    @JsonAlias({"userId", "user_id", "androidId", "android_id", "deviceId", "device_id"})
    @Schema(description = "Android device identifier for Beacon login", example = "a1b2c3d4e5f6g7h8")
    private String userId;

    public boolean hasDeviceId() {
        return userId != null && !userId.isBlank();
    }
}
