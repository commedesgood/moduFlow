package com.moduflow.backend.security;

import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> normalized = OAuth2UserNormalizer.normalize(registrationId, oAuth2User.getAttributes());

        AuthProvider provider = AuthProvider.valueOf((String) normalized.get("provider"));
        String providerId = (String) normalized.get("providerId");
        String email = (String) normalized.get("email");
        String name = (String) normalized.get("name");

        User user = upsertUser(provider, providerId, email, name);
        Map<String, Object> attributes = new LinkedHashMap<>(oAuth2User.getAttributes());
        attributes.put("email", user.getEmail());
        attributes.put("name", user.getName());
        attributes.put("provider", provider.name());
        attributes.put("providerId", providerId);
        attributes.put("role", user.getRole() == UserRole.ADMIN ? "ADMIN" : "USER");

        return new DefaultOAuth2User(
                authoritiesOf(user),
                attributes,
                "email"
        );
    }

    private User upsertUser(AuthProvider provider, String providerId, String email, String name) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> createOrLinkUser(provider, providerId, email, name));
    }

    private User createOrLinkUser(AuthProvider provider, String providerId, String email, String name) {
        return userRepository.findByEmail(email)
                .map(user -> linkExistingUser(user, provider, providerId, name))
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .password("")
                        .name(resolveName(name, null))
                        .provider(provider)
                        .providerId(providerId)
                        .build()));
    }

    private User linkExistingUser(User user, AuthProvider provider, String providerId, String name) {
        AuthProvider current = user.getProvider() == null ? AuthProvider.LOCAL : user.getProvider();
        boolean canLink = current == AuthProvider.LOCAL
                || (current == provider && (user.getProviderId() == null || user.getProviderId().isBlank()));

        if (!canLink) {
            throw oauthException("이미 다른 로그인 방식으로 가입된 이메일입니다.");
        }

        User updated = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(resolveName(name, user.getName()))
                .provider(provider)
                .providerId(providerId)
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return userRepository.save(updated);
    }

    private String resolveName(String socialName, String currentName) {
        if (currentName != null && !currentName.isBlank()) {
            return currentName;
        }
        return socialName == null || socialName.isBlank() ? "User" : socialName;
    }

    private List<SimpleGrantedAuthority> authoritiesOf(User user) {
        UserRole role = user.getRole() == UserRole.ADMIN ? UserRole.ADMIN : UserRole.USER;
        if (role == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private OAuth2AuthenticationException oauthException(String message) {
        return new OAuth2AuthenticationException(new OAuth2Error("social_login_failed", message, null), message);
    }
}
