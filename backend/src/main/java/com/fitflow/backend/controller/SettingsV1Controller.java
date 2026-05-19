package com.fitflow.backend.controller;

import com.fitflow.backend.dto.OkResponse;
import com.fitflow.backend.dto.SettingsRequest;
import com.fitflow.backend.dto.SettingsResponse;
import com.fitflow.backend.security.CustomUserDetails;
import com.fitflow.backend.service.SettingsV1Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "설정", description = "사용자 앱 설정 조회 및 저장 API")
@RequestMapping("/api/v1/settings")
public class SettingsV1Controller {

    private final SettingsV1Service settingsV1Service;

    @GetMapping
    @Operation(summary = "설정 조회", description = "현재 사용자의 자동 출석 설정을 조회합니다.")
    public SettingsResponse get(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return settingsV1Service.get(userDetails.getUserId());
    }

    @PutMapping
    @Operation(summary = "설정 저장", description = "현재 사용자의 자동 출석 설정을 저장합니다.")
    public OkResponse save(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @Valid @RequestBody SettingsRequest request) {
        return settingsV1Service.save(userDetails.getUserId(), request);
    }
}
