package com.moduflow.backend.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class GoogleTokenInfoIdTokenVerifier implements GoogleIdTokenVerifier {

    private static final Set<String> TRUSTED_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final Environment environment;
    private final RestClient restClient;

    public GoogleTokenInfoIdTokenVerifier(Environment environment, RestClient.Builder restClientBuilder) {
        this.environment = environment;
        this.restClient = restClientBuilder.baseUrl("https://oauth2.googleapis.com").build();
    }

    @Override
    public GoogleIdTokenClaims verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new GoogleIdTokenVerificationException("Google ID token is required.");
        }

        GoogleTokenInfoResponse tokenInfo = fetchTokenInfo(idToken);
        validate(tokenInfo);

        return new GoogleIdTokenClaims(
                tokenInfo.subject,
                tokenInfo.email.trim().toLowerCase(Locale.ROOT),
                tokenInfo.name
        );
    }

    private GoogleTokenInfoResponse fetchTokenInfo(String idToken) {
        try {
            GoogleTokenInfoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tokeninfo")
                            .queryParam("id_token", idToken)
                            .build())
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);

            if (response == null) {
                throw new GoogleIdTokenVerificationException("Google tokeninfo returned an empty response.");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new GoogleIdTokenVerificationException("Google ID token was rejected.", exception);
        } catch (RestClientException exception) {
            throw new GoogleIdTokenVerificationException("Failed to verify Google ID token.", exception);
        }
    }

    private void validate(GoogleTokenInfoResponse tokenInfo) {
        if (!TRUSTED_ISSUERS.contains(tokenInfo.issuer)) {
            throw new GoogleIdTokenVerificationException("Google ID token has an invalid issuer.");
        }
        if (isBlank(tokenInfo.subject)) {
            throw new GoogleIdTokenVerificationException("Google ID token is missing subject.");
        }
        if (isBlank(tokenInfo.email)) {
            throw new GoogleIdTokenVerificationException("Google ID token is missing email.");
        }
        if (!isEmailVerified(tokenInfo.emailVerified)) {
            throw new GoogleIdTokenVerificationException("Google account email is not verified.");
        }
        if (tokenInfo.expirationEpochSeconds != null
                && Instant.ofEpochSecond(tokenInfo.expirationEpochSeconds).isBefore(Instant.now())) {
            throw new GoogleIdTokenVerificationException("Google ID token is expired.");
        }

        Set<String> allowedAudiences = allowedAudiences();
        if (allowedAudiences.isEmpty()) {
            throw new IllegalStateException("Configure GOOGLE_CLIENT_ID or GOOGLE_ALLOWED_CLIENT_IDS for Google ID token login.");
        }
        if (!allowedAudiences.contains(tokenInfo.audience)) {
            throw new GoogleIdTokenVerificationException("Google ID token audience is not allowed.");
        }
    }

    private Set<String> allowedAudiences() {
        LinkedHashSet<String> audiences = new LinkedHashSet<>();
        addCsv(audiences, environment.getProperty("app.oauth.google.allowed-client-ids"));
        addCsv(audiences, environment.getProperty("GOOGLE_ALLOWED_CLIENT_IDS"));
        add(audiences, environment.getProperty("spring.security.oauth2.client.registration.google.client-id"));
        add(audiences, environment.getProperty("GOOGLE_CLIENT_ID"));
        add(audiences, environment.getProperty("GOOGLE_ANDROID_CLIENT_ID"));
        return audiences;
    }

    private void addCsv(Set<String> values, String csv) {
        if (csv == null || csv.isBlank()) {
            return;
        }

        for (String value : csv.split(",")) {
            add(values, value);
        }
    }

    private void add(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value.trim());
        }
    }

    private boolean isEmailVerified(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class GoogleTokenInfoResponse {

        @JsonProperty("iss")
        private String issuer;

        @JsonProperty("sub")
        private String subject;

        @JsonProperty("aud")
        private String audience;

        @JsonProperty("email")
        private String email;

        @JsonProperty("email_verified")
        private Object emailVerified;

        @JsonProperty("name")
        private String name;

        @JsonProperty("exp")
        private Long expirationEpochSeconds;
    }
}
