package com.moduflow.backend.service;

import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.security.AdminTestAccountProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminTestAccountInitializer implements ApplicationRunner {

    private final AdminTestAccountProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        String email = normalizeEmail(properties.getEmail());
        String password = properties.getPassword();
        String name = normalizeName(properties.getName());

        validate(email, password);

        User user = userRepository.findByEmail(email)
                .map(existing -> updateExistingAdmin(existing, password, name))
                .orElseGet(() -> createAdmin(email, password, name));

        log.info("Admin test account is ready: email={}, userId={}", user.getEmail(), user.getId());
    }

    private User createAdmin(String email, String password, String name) {
        User admin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .provider(AuthProvider.LOCAL)
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        return userRepository.save(admin);
    }

    private User updateExistingAdmin(User existing, String password, String name) {
        String encodedPassword = existing.getPassword();
        if (encodedPassword == null || encodedPassword.isBlank() || !passwordEncoder.matches(password, encodedPassword)) {
            encodedPassword = passwordEncoder.encode(password);
        }

        User admin = User.builder()
                .id(existing.getId())
                .email(existing.getEmail())
                .password(encodedPassword)
                .name(name)
                .provider(existing.getProvider() == null ? AuthProvider.LOCAL : existing.getProvider())
                .providerId(existing.getProviderId())
                .role(UserRole.ADMIN)
                .active(true)
                .createdAt(existing.getCreatedAt())
                .updatedAt(existing.getUpdatedAt())
                .build();
        return userRepository.save(admin);
    }

    private void validate(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("ADMIN_TEST_EMAIL is required when ADMIN_TEST_ENABLED=true.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("ADMIN_TEST_PASSWORD is required when ADMIN_TEST_ENABLED=true.");
        }
        if (password.length() < 8) {
            throw new IllegalStateException("ADMIN_TEST_PASSWORD must be at least 8 characters.");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name == null || name.isBlank() ? "CMS Admin" : name.trim();
    }
}
