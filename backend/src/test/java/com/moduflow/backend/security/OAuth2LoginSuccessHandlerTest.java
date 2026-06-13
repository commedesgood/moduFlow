package com.moduflow.backend.security;

import com.moduflow.backend.entity.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2LoginSuccessHandlerTest {

    @Test
    void redirectsToFrontendWithAccessTokenWhenJwtGenerationSucceeds() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.generateToken("user@example.com", UserRole.USER)).thenReturn("jwt-token");

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(jwtUtil, frontendProperties());
        HttpServletResponse response = mock(HttpServletResponse.class);

        handler.onAuthenticationSuccess(
                null,
                response,
                new TestingAuthenticationToken("user@example.com", null)
        );

        verify(response).sendRedirect("https://app.example.com/oauth/callback?accessToken=jwt-token");
    }

    @Test
    void redirectsWithAdminRoleTokenWhenSocialUserHasAdminAuthority() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.generateToken("admin@example.com", UserRole.ADMIN)).thenReturn("admin-jwt-token");

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(jwtUtil, frontendProperties());
        HttpServletResponse response = mock(HttpServletResponse.class);

        handler.onAuthenticationSuccess(
                null,
                response,
                new TestingAuthenticationToken(
                        "admin@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        verify(response).sendRedirect("https://app.example.com/oauth/callback?accessToken=admin-jwt-token");
    }

    @Test
    void redirectsToFrontendFailurePathWhenJwtGenerationThrows() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.generateToken("user@example.com", UserRole.USER))
                .thenThrow(new IllegalStateException("Serializer implementation missing"));

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(jwtUtil, frontendProperties());
        HttpServletResponse response = mock(HttpServletResponse.class);

        handler.onAuthenticationSuccess(
                null,
                response,
                new TestingAuthenticationToken("user@example.com", null)
        );

        verify(response).sendRedirect("https://app.example.com/oauth/callback?error=Failed%20to%20finish%20social%20login.");
    }

    private AppFrontendProperties frontendProperties() {
        AppFrontendProperties properties = new AppFrontendProperties();
        properties.setBaseUrl("https://app.example.com");
        properties.setOauthSuccessPath("/oauth/callback");
        properties.setOauthFailurePath("/oauth/callback");
        return properties;
    }
}
