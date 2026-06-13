package com.moduflow.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public class AppCorsProperties {
    private static final List<String> DEFAULT_ALLOWED_ORIGIN_PATTERNS = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*",
            "https://modu-flow-frontend.vercel.app",
            "https://*.vercel.app",
            "https://*.figma.site"
    );

    private List<String> allowedOriginPatterns = DEFAULT_ALLOWED_ORIGIN_PATTERNS;

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()
                ? DEFAULT_ALLOWED_ORIGIN_PATTERNS
                : allowedOriginPatterns;
    }
}
