package com.moduflow.backend.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuth2ClientRegistrationConfig {

    private static final String REDIRECT_URI = "{baseUrl}/login/oauth2/code/{registrationId}";

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    @Conditional(AnyOAuth2ClientConfiguredCondition.class)
    public ClientRegistrationRepository clientRegistrationRepository(Environment environment) {
        List<ClientRegistration> registrations = new ArrayList<>();

        addIfConfigured(registrations, google(environment));
        addIfConfigured(registrations, kakao(environment));
        addIfConfigured(registrations, naver(environment));

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration google(Environment environment) {
        String clientId = property(environment, "google", "client-id", "GOOGLE_CLIENT_ID");
        String clientSecret = property(environment, "google", "client-secret", "GOOGLE_CLIENT_SECRET");
        if (isBlank(clientId) || isBlank(clientSecret)) {
            return null;
        }

        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(REDIRECT_URI)
                .scope("profile", "email")
                .build();
    }

    private ClientRegistration kakao(Environment environment) {
        String clientId = property(environment, "kakao", "client-id", "KAKAO_CLIENT_ID");
        String clientSecret = property(environment, "kakao", "client-secret", "KAKAO_CLIENT_SECRET");
        if (isBlank(clientId) || isBlank(clientSecret)) {
            return null;
        }

        return ClientRegistration.withRegistrationId("kakao")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECT_URI)
                .scope("profile_nickname")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("Kakao")
                .build();
    }

    private ClientRegistration naver(Environment environment) {
        String clientId = property(environment, "naver", "client-id", "NAVER_CLIENT_ID");
        String clientSecret = property(environment, "naver", "client-secret", "NAVER_CLIENT_SECRET");
        if (isBlank(clientId) || isBlank(clientSecret)) {
            return null;
        }

        return ClientRegistration.withRegistrationId("naver")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(REDIRECT_URI)
                .scope("name", "email")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .clientName("Naver")
                .build();
    }

    private static void addIfConfigured(List<ClientRegistration> registrations, ClientRegistration registration) {
        if (registration != null) {
            registrations.add(registration);
        }
    }

    private static String property(Environment environment, String registrationId, String key, String envName) {
        String value = environment.getProperty("spring.security.oauth2.client.registration."
                + registrationId + "." + key);
        return isBlank(value) ? environment.getProperty(envName) : value;
    }

    private static boolean hasClient(Environment environment, String registrationId, String idEnv, String secretEnv) {
        return !isBlank(property(environment, registrationId, "client-id", idEnv))
                && !isBlank(property(environment, registrationId, "client-secret", secretEnv));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    static class AnyOAuth2ClientConfiguredCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return hasClient(environment, "google", "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET")
                    || hasClient(environment, "kakao", "KAKAO_CLIENT_ID", "KAKAO_CLIENT_SECRET")
                    || hasClient(environment, "naver", "NAVER_CLIENT_ID", "NAVER_CLIENT_SECRET");
        }
    }
}
