package com.fitflow.backend.controller;

import com.fitflow.backend.dto.CurrentLocationResponse;
import com.fitflow.backend.dto.LocationRequest;
import com.fitflow.backend.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "비콘 위치", description = "Beacon 앱/프론트 위치 연동 API")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/update-location")
    @Operation(
            operationId = "updateBeaconLocation",
            summary = "비콘 위치 갱신",
            description = """
                    Beacon 앱이 Android ANDROID_ID와 비콘 구역 코드를 전송하면 현재 위치와 이력을 저장합니다.
                    기본 필드는 `userId`, `zoneId`이며, 기기 연동 호환을 위해 `androidId`, `deviceId`, `beaconId`, `minor`도 허용합니다.
                    """
    )
    public ResponseEntity<Void> updateLocation(@RequestBody LocationRequest request) {
        locationService.updateLocation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current-location/{userId}")
    @Operation(
            operationId = "getCurrentBeaconLocation",
            summary = "현재 비콘 위치 조회",
            description = "프론트가 Android ANDROID_ID 기준으로 사용자의 최신 비콘 위치를 조회합니다."
    )
    public CurrentLocationResponse getCurrentLocation(
            @Parameter(description = "Android ANDROID_ID", example = "a1b2c3d4e5f6g7h8")
            @PathVariable String userId) {
        return locationService.getCurrentLocation(userId);
    }
}
