package com.moduflow.backend.controller;

import com.moduflow.backend.dto.DeviceRegistrationRequest;
import com.moduflow.backend.dto.DeviceRegistrationResponse;
import com.moduflow.backend.dto.MeProfileResponse;
import com.moduflow.backend.dto.ProfileNameUpdateRequest;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.DeviceRegistrationService;
import com.moduflow.backend.service.MeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "내 정보", description = "현재 로그인한 사용자 정보 API")
@RequestMapping("/api/v1")
public class MeController {

    private final MeService meService;
    private final DeviceRegistrationService deviceRegistrationService;

    public MeController(MeService meService, DeviceRegistrationService deviceRegistrationService) {
        this.meService = meService;
        this.deviceRegistrationService = deviceRegistrationService;
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 기준으로 현재 로그인한 사용자의 id, email, name을 조회합니다.")
    public MeProfileResponse me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return meService.getMe(userDetails == null ? null : userDetails.getUserId());
    }

    @PatchMapping("/me")
    @Operation(summary = "내 이름 수정", description = "JWT 기준으로 현재 로그인한 사용자의 name을 수정합니다.")
    public MeProfileResponse updateMe(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody ProfileNameUpdateRequest request) {
        return meService.updateName(userDetails == null ? null : userDetails.getUserId(), request);
    }

    @PostMapping("/me/device")
    @Operation(
            summary = "Register Android device",
            description = "Registers the Android ANDROID_ID to the logged-in user for beacon-based automatic attendance."
    )
    public DeviceRegistrationResponse registerDevice(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestBody DeviceRegistrationRequest request) {
        return deviceRegistrationService.register(userDetails == null ? null : userDetails.getUserId(), request);
    }
}
