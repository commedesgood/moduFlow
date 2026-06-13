package com.moduflow.backend.security;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2UserNormalizerTest {

    @Test
    void kakaoAllowsMissingKakaoAccountAndUsesSyntheticEmail() {
        Map<String, Object> normalized = OAuth2UserNormalizer.normalize("kakao", Map.of(
                "id", 12345L,
                "properties", Map.of("nickname", "카카오사용자")
        ));

        assertThat(normalized.get("provider")).isEqualTo("KAKAO");
        assertThat(normalized.get("providerId")).isEqualTo("12345");
        assertThat(normalized.get("email")).isEqualTo("kakao-12345@social.moduflow.local");
        assertThat(normalized.get("name")).isEqualTo("카카오사용자");
    }

    @Test
    void googleReadsEmailAndName() {
        Map<String, Object> normalized = OAuth2UserNormalizer.normalize("google", Map.of(
                "sub", "google-id",
                "email", "user@example.com",
                "name", "Google User"
        ));

        assertThat(normalized.get("provider")).isEqualTo("GOOGLE");
        assertThat(normalized.get("providerId")).isEqualTo("google-id");
        assertThat(normalized.get("email")).isEqualTo("user@example.com");
        assertThat(normalized.get("name")).isEqualTo("Google User");
    }
}
