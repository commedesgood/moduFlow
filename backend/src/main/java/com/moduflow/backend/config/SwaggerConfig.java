package com.moduflow.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(@Value("${app.server.base-url:}") String serverBaseUrl) {
        String jwtSchemeName = "bearerAuth";

        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("moduflow API - 모두의 플로우")
                        .version("1.0")
                        .description("""
                                moduflow(모두의 플로우) 백엔드 API 문서입니다.

                                작업자 연계 설정은 `GET /api/v1/integration-settings`에서 확인합니다.
                                이 응답에는 프론트 OAuth callback, JWT 저장 위치, Beacon 요청 필드명, CORS origin 패턴, 운영 환경변수 이름이 정리되어 있습니다.

                                인증이 필요한 API는 Swagger UI 우측 상단 Authorize 버튼에 `Bearer {accessToken}` 형식이 아닌 토큰 값만 입력하면 됩니다.
                                프론트엔드는 로그인 성공 후 받은 accessToken을 `sessionStorage.auth_token`에 저장하고 `Authorization: Bearer {token}` 헤더로 호출합니다.
                                """))
                .addTagsItem(new Tag().name("연계 설정").description("프론트엔드, Beacon, OAuth, CORS 작업자가 함께 보는 설정 요약"))
                .addTagsItem(new Tag().name("소셜 로그인").description("브라우저 리다이렉트 기반 OAuth2 로그인 계약"))
                .addTagsItem(new Tag().name("인증").description("회원가입, 로그인, 토큰 발급, 비밀번호 변경"))
                .addTagsItem(new Tag().name("출석").description("JWT 로그인 사용자 기준 출석/퇴장 및 헬스장 혼잡도"))
                .addTagsItem(new Tag().name("운동기록").description("날짜별 운동기록, 카운트, 운동 아이템"))
                .addTagsItem(new Tag().name("루틴 설정").description("요일별 운동 루틴"))
                .addTagsItem(new Tag().name("통계").description("월간 운동 요약"))
                .addTagsItem(new Tag().name("설정").description("사용자 앱 설정"))
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName,
                                new SecurityScheme()
                                        .name(jwtSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                        .addSchemas("ErrorResponse", errorResponseSchema())
                );

        if (serverBaseUrl != null && !serverBaseUrl.isBlank()) {
            api.addServersItem(new Server().url(serverBaseUrl));
        }

        return api;
    }

    @Bean
    public OpenApiCustomizer frontendContractCustomizer() {
        return openApi -> {
            addOAuthAuthorizationContract(openApi);
            markPublicOperations(openApi);
            addCommonErrorResponses(openApi);
        };
    }

    private Schema<?> errorResponseSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("code", new StringSchema()
                .description("에러 코드")
                .example("VALIDATION_ERROR"));
        schema.addProperty("message", new StringSchema()
                .description("사용자에게 표시 가능한 에러 메시지")
                .example("name은 필수입니다."));
        schema.setRequired(List.of("code", "message"));
        return schema;
    }

    private void addOAuthAuthorizationContract(OpenAPI openApi) {
        openApi.path("/oauth2/authorization/{provider}", new PathItem()
                .get(new Operation()
                        .tags(List.of("소셜 로그인"))
                        .summary("소셜 로그인 시작")
                        .description("""
                                프론트 로그인 버튼은 브라우저를 이 URL로 이동시킵니다.

                                지원 provider: `google`, `kakao`, `naver`

                                성공 시 백엔드는 `{FRONTEND_URL}/oauth/callback?accessToken={JWT}`로 redirect 합니다.
                                실패 시 백엔드는 `{FRONTEND_URL}/oauth/callback?error={message}`로 redirect 합니다.

                                이 API는 JSON을 반환하지 않고 provider 인증 페이지 또는 프론트 콜백으로 302 redirect 됩니다.
                                """)
                        .operationId("startSocialLogin")
                        .addParametersItem(new Parameter()
                                .name("provider")
                                .in("path")
                                .required(true)
                                .description("OAuth provider")
                                .schema(providerSchema()))
                        .responses(new ApiResponses()
                                .addApiResponse("302", new ApiResponse()
                                        .description("Provider 인증 페이지 또는 프론트 콜백으로 redirect")
                                        .content(new Content().addMediaType("text/html", new MediaType())))
                                .addApiResponse("400", new ApiResponse()
                                        .description("지원하지 않는 provider 또는 OAuth 요청 실패")))
                        .security(List.of())));
    }

    private void markPublicOperations(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            if (isPublicPath(path)) {
                operation.setSecurity(List.of());
            }
        }));
    }

    private void addCommonErrorResponses(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
                return;
            }

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            responses.addApiResponse("400", responses.getOrDefault("400", errorResponse("잘못된 JSON 또는 요청 형식")));
            responses.addApiResponse("422", responses.getOrDefault("422", errorResponse("입력값 검증 실패")));
            responses.addApiResponse("500", responses.getOrDefault("500", errorResponse("서버 오류")));

            if (!isPublicPath(path)) {
                responses.addApiResponse("401", responses.getOrDefault("401", errorResponse("인증 필요 또는 유효하지 않은 JWT")));
                responses.addApiResponse("403", responses.getOrDefault("403", errorResponse("접근 권한 없음")));
            }
        }));
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(errorResponseRef())));
    }

    private StringSchema providerSchema() {
        StringSchema schema = new StringSchema();
        schema.setEnum(List.of("google", "kakao", "naver"));
        return schema;
    }

    private Schema<Object> errorResponseRef() {
        Schema<Object> schema = new Schema<>();
        schema.set$ref("#/components/schemas/ErrorResponse");
        return schema;
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/api/v1/auth/signup")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/integration-settings")
                || path.equals("/auth/signup")
                || path.equals("/auth/login")
                || path.equals("/ping")
                || path.equals("/api/update-location")
                || path.equals("/api/v1/update-location")
                || path.startsWith("/api/current-location/")
                || path.startsWith("/api/v1/current-location/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

}
