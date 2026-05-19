package com.fitflow.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void customizerAddsOAuthContractAndCommonErrors() {
        OpenAPI openApi = swaggerConfig.openAPI("");
        openApi.path("/api/v1/workouts", new PathItem()
                .get(new Operation()
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("OK")))));
        openApi.path("/api/v1/integration-settings", new PathItem()
                .get(new Operation()
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("OK")))));
        openApi.path("/api/update-location", new PathItem()
                .post(new Operation()
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("OK")))));
        openApi.path("/api/current-location/{userId}", new PathItem()
                .get(new Operation()
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("OK")))));

        swaggerConfig.frontendContractCustomizer().customise(openApi);

        assertThat(openApi.getComponents().getSchemas()).containsKey("ErrorResponse");
        assertThat(openApi.getInfo().getDescription()).contains("/api/v1/integration-settings");
        assertThat(openApi.getTags()).extracting("name").contains("연계 설정");
        assertThat(openApi.getPaths()).containsKey("/oauth2/authorization/{provider}");
        assertThat(openApi.getPaths().get("/oauth2/authorization/{provider}").getGet().getSecurity()).isEmpty();
        assertThat(openApi.getPaths().get("/api/v1/integration-settings").getGet().getSecurity()).isEmpty();
        assertThat(openApi.getPaths().get("/api/update-location").getPost().getSecurity()).isEmpty();
        assertThat(openApi.getPaths().get("/api/current-location/{userId}").getGet().getSecurity()).isEmpty();
        assertThat(openApi.getPaths().get("/api/v1/workouts").getGet().getResponses())
                .containsKeys("400", "401", "403", "422", "500");
        assertThat(openApi.getPaths().get("/api/update-location").getPost().getResponses())
                .doesNotContainKeys("401", "403");
    }
}
