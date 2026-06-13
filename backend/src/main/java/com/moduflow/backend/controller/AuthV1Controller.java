package com.moduflow.backend.controller;

import com.moduflow.backend.dto.AuthLoginRequest;
import com.moduflow.backend.dto.AuthResponse;
import com.moduflow.backend.dto.AuthSignupRequest;
import com.moduflow.backend.dto.GoogleIdTokenLoginRequest;
import com.moduflow.backend.dto.OkResponse;
import com.moduflow.backend.dto.PasswordChangeRequest;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.AuthV1Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 비밀번호 변경 API")
@RequestMapping("/api/v1/auth")
public class AuthV1Controller {

    private final AuthV1Service authV1Service;

    @PostMapping("/signup")
    @Operation(
            operationId = "authSignup",
            summary = "회원가입",
            description = """
                    이메일과 비밀번호로 새 사용자를 생성하고 accessToken을 발급합니다.
                    프론트는 응답의 `accessToken`을 `sessionStorage.auth_token`에 저장합니다.
                    """
    )
    public AuthResponse signup(@Valid @RequestBody AuthSignupRequest request) {
        return authV1Service.signup(request);
    }

    @PostMapping("/login")
    @Operation(
            operationId = "authLogin",
            summary = "로그인",
            description = """
                    이메일과 비밀번호를 검증하고 accessToken을 발급합니다.
                    프론트는 응답의 `accessToken`을 `sessionStorage.auth_token`에 저장합니다.
                    """
    )
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authV1Service.login(request);
    }

    @PostMapping("/google")
    @Operation(
            operationId = "authGoogleIdTokenLogin",
            summary = "Android Google ID token login",
            description = """
                    Android clients should sign in with the native Google flow and POST the returned ID token here.
                    This avoids opening Google OAuth inside an embedded WebView.
                    """
    )
    public AuthResponse googleLogin(@Valid @RequestBody GoogleIdTokenLoginRequest request) {
        return authV1Service.loginWithGoogleIdToken(request);
    }

    @PatchMapping("/password")
    @Operation(
            operationId = "changePassword",
            summary = "비밀번호 변경",
            description = "JWT 로그인 사용자의 현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다."
    )
    public OkResponse changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @Valid @RequestBody PasswordChangeRequest request) {
        return authV1Service.changePassword(userDetails.getUserId(), request);
    }
}
