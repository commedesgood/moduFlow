package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "비콘 구역 변경 요청")
public class LocationRequest {

    @JsonAlias({"user_id", "androidId", "android_id", "deviceId", "device_id"})
    @Schema(description = "Android ANDROID_ID. Beacon 앱에서 deviceId/androidId로 보내도 처리합니다.", example = "a1b2c3d4e5f6g7h8")
    private String userId;

    @JsonAlias({"zone_id", "beaconId", "beacon_id", "minor", "beaconMinor", "zoneCode", "zone_code"})
    @Schema(description = "비콘 구역 코드. Beacon minor/beaconId로 보내도 처리합니다.", example = "53626")
    private Integer zoneId;

    @JsonAlias({"gym_name", "gym"})
    @Schema(description = "Gym name used to aggregate beacon congestion", example = "ModuFlow")
    private String gymName;

    public String getUserId() {
        return userId;
    }

    public Integer getZoneId() {
        return zoneId;
    }

    public String getGymName() {
        return gymName;
    }
}
