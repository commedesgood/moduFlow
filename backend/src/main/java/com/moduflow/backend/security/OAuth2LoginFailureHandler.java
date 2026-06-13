package com.moduflow.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final AppFrontendProperties frontendProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String message = resolveMessage(exception);
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendProperties.getBaseUrl())
                .path(frontendProperties.getOauthFailurePath())
                .queryParam("error", message)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String resolveMessage(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauthException
                && oauthException.getError().getDescription() != null
                && !oauthException.getError().getDescription().isBlank()) {
            return oauthException.getError().getDescription();
        }
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "소셜 로그인 실패"
                : exception.getMessage();
    }
}
