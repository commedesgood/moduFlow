package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Android device registration request")
public class DeviceRegistrationRequest {

    @JsonAlias({"userId", "user_id", "androidId", "android_id", "deviceId", "device_id"})
    @Schema(description = "Android ANDROID_ID", example = "a1b2c3d4e5f6g7h8")
    private String androidId;
}
