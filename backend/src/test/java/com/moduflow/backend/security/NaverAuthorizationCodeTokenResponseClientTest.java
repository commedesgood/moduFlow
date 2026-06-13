package com.moduflow.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NaverAuthorizationCodeTokenResponseClientTest {

    @Test
    void parsesNaverTokenResponseUsingGetRequest() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(jsonAcceptInterceptor());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(once(), requestTo(containsString("https://nid.naver.com/oauth2.0/token")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "access_token": "naver-access-token",
                          "refresh_token": "naver-refresh-token",
                          "token_type": "bearer",
                          "expires_in": "3600"
                        }
                        """, MediaType.APPLICATION_JSON));

        NaverAuthorizationCodeTokenResponseClient client =
                new NaverAuthorizationCodeTokenResponseClient(req -> {
                    throw new AssertionError("Delegate should not be used for naver.");
                }, restTemplate, new ObjectMapper());

        OAuth2AccessTokenResponse response = client.getTokenResponse(naverGrantRequest());

        assertThat(response.getAccessToken().getTokenValue()).isEqualTo("naver-access-token");
        assertThat(response.getRefreshToken().getTokenValue()).isEqualTo("naver-refresh-token");
        assertThat(response.getAccessToken().getTokenType()).isEqualTo(OAuth2AccessToken.TokenType.BEARER);
        assertThat(response.getAccessToken().getScopes()).containsExactlyInAnyOrder("email", "name");

        server.verify();
    }

    @Test
    void surfacesNaverErrorDescriptionFromTokenEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(jsonAcceptInterceptor());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(once(), requestTo(containsString("https://nid.naver.com/oauth2.0/token")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "error": "server_error",
                          "error_description": "invalid client"
                        }
                        """, MediaType.APPLICATION_JSON));

        NaverAuthorizationCodeTokenResponseClient client =
                new NaverAuthorizationCodeTokenResponseClient(req -> {
                    throw new AssertionError("Delegate should not be used for naver.");
                }, restTemplate, new ObjectMapper());

        OAuth2AuthenticationException exception =
                catchThrowableOfType(() -> client.getTokenResponse(naverGrantRequest()), OAuth2AuthenticationException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getError().getDescription()).contains("invalid client");

        server.verify();
    }

    @Test
    void stripsControlCharactersFromTokenRequestQueryParameters() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(jsonAcceptInterceptor());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(once(), requestTo(not(containsString("%0D"))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "access_token": "naver-access-token",
                          "token_type": "bearer",
                          "expires_in": "3600"
                        }
                        """, MediaType.APPLICATION_JSON));

        NaverAuthorizationCodeTokenResponseClient client =
                new NaverAuthorizationCodeTokenResponseClient(req -> {
                    throw new AssertionError("Delegate should not be used for naver.");
                }, restTemplate, new ObjectMapper());

        OAuth2AccessTokenResponse response = client.getTokenResponse(
                naverGrantRequest("naver-code\r\n", "naver-state\r")
        );

        assertThat(response.getAccessToken().getTokenValue()).isEqualTo("naver-access-token");

        server.verify();
    }

    private static OAuth2AuthorizationCodeGrantRequest naverGrantRequest() {
        return naverGrantRequest("naver-code", "naver-state");
    }

    private static OAuth2AuthorizationCodeGrantRequest naverGrantRequest(String code, String state) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("naver")
                .clientId("naver-client-id")
                .clientSecret("naver-client-secret")
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://3-39-194-42.sslip.io/login/oauth2/code/naver")
                .scope("email", "name")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .build();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .clientId("naver-client-id")
                .redirectUri("https://3-39-194-42.sslip.io/login/oauth2/code/naver")
                .state(state)
                .scope("email", "name")
                .attributes(attrs -> attrs.put(OAuth2ParameterNames.REGISTRATION_ID, "naver"))
                .build();

        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse.success(code)
                .redirectUri("https://3-39-194-42.sslip.io/login/oauth2/code/naver")
                .state(state)
                .build();

        return new OAuth2AuthorizationCodeGrantRequest(
                registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
        );
    }

    private static ClientHttpRequestInterceptor jsonAcceptInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            return execution.execute(request, body);
        };
    }
}
