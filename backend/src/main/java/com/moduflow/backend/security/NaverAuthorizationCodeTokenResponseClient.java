package com.moduflow.backend.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NaverAuthorizationCodeTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate;
    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;

    public NaverAuthorizationCodeTokenResponseClient(ObjectMapper objectMapper) {
        this(new DefaultAuthorizationCodeTokenResponseClient(), buildRestTemplate(), objectMapper);
    }

    NaverAuthorizationCodeTokenResponseClient(
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> delegate,
            RestOperations restOperations,
            ObjectMapper objectMapper
    ) {
        this.delegate = delegate;
        this.restOperations = restOperations;
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        if (!"naver".equalsIgnoreCase(registrationId)) {
            return delegate.getTokenResponse(request);
        }

        URI uri = UriComponentsBuilder
                .fromUriString(request.getClientRegistration().getProviderDetails().getTokenUri())
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", request.getClientRegistration().getClientId())
                .queryParam("client_secret", request.getClientRegistration().getClientSecret())
                .queryParam(
                        "code",
                        sanitizeQueryParam(request.getAuthorizationExchange().getAuthorizationResponse().getCode())
                )
                .queryParam(
                        "state",
                        sanitizeQueryParam(request.getAuthorizationExchange().getAuthorizationRequest().getState())
                )
                .queryParamIfPresent(
                        "redirect_uri",
                        java.util.Optional.ofNullable(request.getAuthorizationExchange()
                                .getAuthorizationRequest()
                                .getRedirectUri())
                                .map(this::sanitizeQueryParam)
                                .filter(StringUtils::hasText)
                )
                .build(true)
                .toUri();

        try {
            RequestEntity<Void> entity = RequestEntity
                    .get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();
            ResponseEntity<String> response = restOperations.exchange(entity, String.class);
            return toAccessTokenResponse(readMap(response.getBody()), request);
        } catch (OAuth2AuthenticationException exception) {
            throw exception;
        } catch (HttpStatusCodeException exception) {
            throw oauthException(readMap(exception.getResponseBodyAsString()), exception);
        } catch (Exception exception) {
            throw oauthException(Map.of(
                    "error", "invalid_token_response",
                    "error_description", "Failed to retrieve Naver access token."
            ), exception);
        }
    }

    private OAuth2AccessTokenResponse toAccessTokenResponse(
            Map<String, Object> body,
            OAuth2AuthorizationCodeGrantRequest request
    ) {
        if (body.containsKey("error")) {
            throw oauthException(body, null);
        }

        String accessToken = stringValue(body.get("access_token"));
        if (!StringUtils.hasText(accessToken)) {
            throw oauthException(Map.of(
                    "error", "invalid_token_response",
                    "error_description", "Naver token response did not include access_token."
            ), null);
        }

        String tokenType = stringValue(body.get("token_type"));
        long expiresIn = longValue(body.get("expires_in"), 3600L);
        String refreshToken = stringValue(body.get("refresh_token"));
        Set<String> scopes = resolveScopes(body.get("scope"), request);

        Map<String, Object> additionalParameters = new LinkedHashMap<>(body);
        additionalParameters.remove("access_token");
        additionalParameters.remove("token_type");
        additionalParameters.remove("expires_in");
        additionalParameters.remove("refresh_token");
        additionalParameters.remove("scope");

        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .additionalParameters(additionalParameters);

        if (StringUtils.hasText(refreshToken)) {
            builder.refreshToken(refreshToken);
        }
        if (StringUtils.hasText(tokenType)) {
            additionalParameters.putIfAbsent("naver_token_type", tokenType);
        }

        return builder.build();
    }

    private Map<String, Object> readMap(String body) {
        if (!StringUtils.hasText(body)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(body, MAP_TYPE);
        } catch (Exception exception) {
            return Map.of(
                    "error", "invalid_token_response",
                    "error_description", body
            );
        }
    }

    private Set<String> resolveScopes(Object scopeValue, OAuth2AuthorizationCodeGrantRequest request) {
        String scope = stringValue(scopeValue);
        if (StringUtils.hasText(scope)) {
            return List.of(scope.split("[ ,]+")).stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        }
        return request.getClientRegistration().getScopes();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String sanitizeQueryParam(String value) {
        if (value == null) {
            return null;
        }

        StringBuilder sanitized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!Character.isISOControl(ch)) {
                sanitized.append(ch);
            }
        }
        return sanitized.toString().strip();
    }

    private long longValue(Object value, long fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private OAuth2AuthenticationException oauthException(Map<String, Object> body, Exception cause) {
        String code = stringValue(body.getOrDefault("error", "invalid_token_response"));
        String description = stringValue(body.get("error_description"));
        if (!StringUtils.hasText(description)) {
            description = stringValue(body.get("message"));
        }
        if (!StringUtils.hasText(description)) {
            description = "Naver login failed while retrieving the access token response.";
        }

        OAuth2Error error = new OAuth2Error(code, description, null);
        return cause == null
                ? new OAuth2AuthenticationException(error, description)
                : new OAuth2AuthenticationException(error, description, cause);
    }

    private static RestTemplate buildRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
