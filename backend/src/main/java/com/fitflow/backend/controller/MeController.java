package com.fitflow.backend.controller;

import com.fitflow.backend.dto.UserInfoResponse;
import com.fitflow.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "내 정보", description = "현재 로그인한 사용자 정보 API")
@RequestMapping("/api/v1")
public class MeController {

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 기준으로 현재 로그인한 사용자의 id, email, name을 조회합니다.")
    public UserInfoResponse me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new UserInfoResponse("u_" + userDetails.getUserId(), userDetails.getEmail(), userDetails.getName());
    }
}
