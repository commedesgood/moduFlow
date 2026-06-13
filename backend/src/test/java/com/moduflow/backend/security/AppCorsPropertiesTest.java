package com.moduflow.backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppCorsPropertiesTest {

    @Test
    void defaultOriginsIncludeDeployedFrontend() {
        AppCorsProperties properties = new AppCorsProperties();

        assertThat(properties.getAllowedOriginPatterns())
                .contains(
                        "https://modu-flow-frontend.vercel.app",
                        "https://*.vercel.app"
                );
    }
}
