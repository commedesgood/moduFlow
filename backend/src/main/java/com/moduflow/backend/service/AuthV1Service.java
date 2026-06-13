package com.moduflow.backend.service;

import com.moduflow.backend.dto.AuthLoginRequest;
import com.moduflow.backend.dto.AuthResponse;
import com.moduflow.backend.dto.AuthSignupRequest;
import com.moduflow.backend.dto.GoogleIdTokenLoginRequest;
import com.moduflow.backend.dto.OkResponse;
import com.moduflow.backend.dto.PasswordChangeRequest;
import com.moduflow.backend.dto.UserInfoResponse;
import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.security.GoogleIdTokenClaims;
import com.moduflow.backend.security.GoogleIdTokenVerificationException;
import com.moduflow.backend.security.GoogleIdTokenVerifier;
import com.moduflow.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthV1Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DeviceRegistrationService deviceRegistrationService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthResponse signup(AuthSignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "Email already exists.");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .name(normalizeDisplayName(request.getName()))
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole());
        return new AuthResponse(token, toUserInfo(saved));
    }

    public AuthResponse login(AuthLoginRequest request) {
        return loginWithEmailAndPassword(request);
    }

    @Transactional
    public AuthResponse loginWithGoogleIdToken(GoogleIdTokenLoginRequest request) {
        GoogleIdTokenClaims claims = verifyGoogleIdToken(request.getIdToken());
        User user = upsertGoogleUser(claims);

        if (request.hasDeviceId()) {
            deviceRegistrationService.register(user.getId(), request.getUserId());
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, toUserInfo(user));
    }

    @Transactional
    public OkResponse changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "User was not found."));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "PASSWORD_UNAVAILABLE", "Password cannot be changed for this account.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "New passwords do not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "New password must be different from current password.");
        }

        User updated = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(passwordEncoder.encode(request.getNewPassword()))
                .name(user.getName())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        userRepository.save(updated);
        return new OkResponse(true);
    }

    private AuthResponse loginWithEmailAndPassword(AuthLoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Invalid email or password."));

        if (user.getPassword() == null
                || user.getPassword().isBlank()
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Invalid email or password.");
        }

        if (request.hasDeviceId()) {
            deviceRegistrationService.register(user.getId(), request.getUserId());
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, toUserInfo(user));
    }

    private GoogleIdTokenClaims verifyGoogleIdToken(String idToken) {
        try {
            return googleIdTokenVerifier.verify(idToken);
        } catch (GoogleIdTokenVerificationException exception) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Invalid Google ID token.");
        }
    }

    private User upsertGoogleUser(GoogleIdTokenClaims claims) {
        return userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, claims.subject())
                .orElseGet(() -> createOrLinkGoogleUser(claims));
    }

    private User createOrLinkGoogleUser(GoogleIdTokenClaims claims) {
        return userRepository.findByEmail(claims.email())
                .map(user -> linkExistingGoogleUser(user, claims))
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(claims.email())
                        .password("")
                        .name(resolveDisplayName(claims.name(), null))
                        .provider(AuthProvider.GOOGLE)
                        .providerId(claims.subject())
                        .build()));
    }

    private User linkExistingGoogleUser(User user, GoogleIdTokenClaims claims) {
        AuthProvider currentProvider = user.getProvider() == null ? AuthProvider.LOCAL : user.getProvider();
        String currentProviderId = user.getProviderId();

        if (currentProvider == AuthProvider.GOOGLE && claims.subject().equals(currentProviderId)) {
            return user;
        }

        boolean canLink = currentProvider == AuthProvider.LOCAL
                || (currentProvider == AuthProvider.GOOGLE
                && (currentProviderId == null || currentProviderId.isBlank()));

        if (!canLink) {
            throw new CustomException(HttpStatus.CONFLICT, "SOCIAL_LOGIN_CONFLICT",
                    "Email is already registered with another login method.");
        }

        User updated = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(resolveDisplayName(claims.name(), user.getName()))
                .provider(AuthProvider.GOOGLE)
                .providerId(claims.subject())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return userRepository.save(updated);
    }

    private UserInfoResponse toUserInfo(User user) {
        UserRole role = user.getRole() == UserRole.ADMIN ? UserRole.ADMIN : UserRole.USER;
        return new UserInfoResponse("u_" + user.getId(), user.getEmail(), user.getName(), role.name());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String name) {
        if (name == null || name.isBlank()) {
            return "User";
        }
        return name.trim();
    }

    private String resolveDisplayName(String socialName, String currentName) {
        if (currentName != null && !currentName.isBlank()) {
            return currentName;
        }
        return normalizeDisplayName(socialName);
    }

}
