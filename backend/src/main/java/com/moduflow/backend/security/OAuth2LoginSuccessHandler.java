package com.moduflow.backend.security;

import com.moduflow.backend.entity.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String POST_LOGIN_FAILURE_MESSAGE = "Failed to finish social login.";

    private final JwtUtil jwtUtil;
    private final AppFrontendProperties frontendProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            String email = authentication.getName();
            String token = jwtUtil.generateToken(email, roleOf(authentication));
            response.sendRedirect(successRedirectUrl(token));
        } catch (RuntimeException exception) {
            log.error("OAuth2 login succeeded but final sign-in step failed", exception);
            response.sendRedirect(failureRedirectUrl(POST_LOGIN_FAILURE_MESSAGE));
        }
    }

    private UserRole roleOf(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean admin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority) || "ADMIN".equals(authority));
            if (admin) {
                return UserRole.ADMIN;
            }
        }
        return UserRole.USER;
    }

    private String successRedirectUrl(String token) {
        return UriComponentsBuilder
                .fromUriString(frontendProperties.getBaseUrl())
                .path(frontendProperties.getOauthSuccessPath())
                .queryParam("accessToken", token)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    private String failureRedirectUrl(String message) {
        return UriComponentsBuilder
                .fromUriString(frontendProperties.getBaseUrl())
                .path(frontendProperties.getOauthFailurePath())
                .queryParam("error", message)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }
}
