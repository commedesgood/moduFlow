package com.fitflow.backend.controller;

import com.fitflow.backend.dto.ApiResponse;
import com.fitflow.backend.dto.MeResponse;
import com.fitflow.backend.dto.SignupRequest;
import com.fitflow.backend.dto.TokenResponse;
import com.fitflow.backend.security.JwtProperties;
import com.fitflow.backend.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.fitflow.backend.dto.LoginRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Hidden
@Deprecated(since = "1.0", forRemoval = false)
public class UserController {

    private final UserService userService;
    private final JwtProperties jwtProperties;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return new ApiResponse<>(200, "회원가입 성공", null);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);

        TokenResponse data = new TokenResponse(
                token,
                "Bearer",
                jwtProperties.getExpiration().toSeconds(),
                request.getEmail()
        );

        return new ApiResponse<>(200, "로그인 성공", data);
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        return new ApiResponse<>(200, "조회 성공", new MeResponse(authentication.getName()));
    }
}
