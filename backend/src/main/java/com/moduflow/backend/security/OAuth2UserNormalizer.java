package com.moduflow.backend.security;

import com.moduflow.backend.entity.AuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class OAuth2UserNormalizer {

    private static final String ERROR_CODE = "invalid_social_user_info";

    private OAuth2UserNormalizer() {
    }

    public static Map<String, Object> normalize(String registrationId, Map<String, Object> attributes) {
        OAuth2Provider provider;
        try {
            provider = OAuth2Provider.valueOf(registrationId.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw oauthException("지원하지 않는 소셜 로그인입니다.");
        }

        return switch (provider) {
            case GOOGLE -> normalizeGoogle(provider.toAuthProvider(), attributes);
            case KAKAO -> normalizeKakao(provider.toAuthProvider(), attributes);
            case NAVER -> normalizeNaver(provider.toAuthProvider(), attributes);
        };
    }

    private static Map<String, Object> normalizeGoogle(AuthProvider provider, Map<String, Object> attributes) {
        String providerId = stringValue(attributes.get("sub"));
        String email = stringValue(attributes.get("email"));
        String name = firstNonBlank(
                stringValue(attributes.get("name")),
                stringValue(attributes.get("given_name"))
        );
        return normalized(provider, providerId, email, name, attributes);
    }

    private static Map<String, Object> normalizeKakao(AuthProvider provider, Map<String, Object> attributes) {
        String providerId = stringValue(attributes.get("id"));

        Map<String, Object> kakaoAccount = asMapOrEmpty(attributes.get("kakao_account"));
        Map<String, Object> properties = asMapOrEmpty(attributes.get("properties"));
        Map<String, Object> profile = asMapOrEmpty(kakaoAccount.get("profile"));

        String email = firstNonBlank(
                stringValue(kakaoAccount.get("email")),
                socialEmail(provider, providerId)
        );
        String name = firstNonBlank(
                stringValue(properties.get("nickname")),
                stringValue(profile.get("nickname"))
        );

        return normalized(provider, providerId, email, name, attributes);
    }

    private static Map<String, Object> normalizeNaver(AuthProvider provider, Map<String, Object> attributes) {
        Map<String, Object> responseMap = asMap(attributes.get("response"), "네이버 계정 정보를 가져오지 못했습니다.");
        String providerId = stringValue(responseMap.get("id"));
        String email = stringValue(responseMap.get("email"));
        String name = firstNonBlank(
                stringValue(responseMap.get("name")),
                stringValue(responseMap.get("nickname"))
        );
        return normalized(provider, providerId, email, name, responseMap);
    }

    private static Map<String, Object> normalized(AuthProvider provider,
                                                  String providerId,
                                                  String email,
                                                  String name,
                                                  Map<String, Object> rawAttributes) {
        if (providerId == null || providerId.isBlank()) {
            throw oauthException("소셜 로그인 식별자를 가져오지 못했습니다.");
        }
        if (email == null || email.isBlank()) {
            throw oauthException("이메일 제공 동의가 필요합니다.");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("provider", provider.name());
        map.put("providerId", providerId);
        map.put("email", email);
        map.put("name", defaultName(name));
        map.put("raw", rawAttributes);
        return map;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value, String errorMessage) {
        if (!(value instanceof Map<?, ?>)) {
            throw oauthException(errorMessage);
        }
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMapOrEmpty(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            return Map.of();
        }
        return (Map<String, Object>) value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String defaultName(String name) {
        return name == null || name.isBlank() ? "User" : name;
    }

    private static String socialEmail(AuthProvider provider, String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return null;
        }
        String safeProviderId = providerId.replaceAll("[^A-Za-z0-9._-]", "_");
        return provider.name().toLowerCase(Locale.ROOT) + "-" + safeProviderId + "@social.moduflow.local";
    }

    private static OAuth2AuthenticationException oauthException(String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(ERROR_CODE, message, null), message);
    }
}
