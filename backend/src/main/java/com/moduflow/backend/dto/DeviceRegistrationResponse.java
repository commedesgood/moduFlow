package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Android device registration response")
public record DeviceRegistrationResponse(
        @Schema(description = "Logged-in user id", example = "1")
        Long userId,

        @Schema(description = "Masked Android device id", example = "a1b2********g7h8")
        String maskedAndroidId,

        @Schema(description = "Registration update time")
        Instant registeredAt
) {
}
