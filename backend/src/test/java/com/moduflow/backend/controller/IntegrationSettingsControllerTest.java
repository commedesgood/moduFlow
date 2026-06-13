package com.moduflow.backend.controller;

import com.moduflow.backend.security.AppCorsProperties;
import com.moduflow.backend.security.AppFrontendProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IntegrationSettingsControllerTest {

    @Test
    void returnsIntegrationSettingsWithoutSecrets() throws Exception {
        AppFrontendProperties frontendProperties = new AppFrontendProperties();
        frontendProperties.setBaseUrl("https://app.example.com");

        AppCorsProperties corsProperties = new AppCorsProperties();
        corsProperties.setAllowedOriginPatterns(List.of("https://app.example.com", "https://*.figma.site"));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new IntegrationSettingsController(
                        frontendProperties,
                        corsProperties,
                        "https://api.example.com"
                ))
                .build();

        mockMvc.perform(get("/api/v1/integration-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverBaseUrl").value("https://api.example.com"))
                .andExpect(jsonPath("$.frontend.baseUrl").value("https://app.example.com"))
                .andExpect(jsonPath("$.frontend.oauthSuccessRedirect").value("https://app.example.com/oauth/callback?accessToken={JWT}"))
                .andExpect(jsonPath("$.auth.tokenStorageKey").value("sessionStorage.auth_token"))
                .andExpect(jsonPath("$.auth.authorizationHeader").value("Authorization: Bearer {accessToken}"))
                .andExpect(jsonPath("$.beacon.updateLocationPath").value("POST /api/v1/update-location"))
                .andExpect(jsonPath("$.beacon.userIdFields[2]").value("androidId"))
                .andExpect(jsonPath("$.beacon.zoneIdFields[4]").value("minor"))
                .andExpect(jsonPath("$.beacon.zoneMappings[0].zoneId").value(53626))
                .andExpect(jsonPath("$.beacon.publicAccess").value(true))
                .andExpect(jsonPath("$.cors.allowedOriginPatterns[1]").value("https://*.figma.site"))
                .andExpect(jsonPath("$.runtimeProperties[3].env").value("JWT_SECRET"))
                .andExpect(jsonPath("$.runtimeProperties[3].secret").value(true));
    }
}
