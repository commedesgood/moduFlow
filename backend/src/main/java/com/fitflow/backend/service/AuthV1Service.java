package com.fitflow.backend.service;

import com.fitflow.backend.dto.AuthLoginRequest;
import com.fitflow.backend.dto.AuthResponse;
import com.fitflow.backend.dto.AuthSignupRequest;
import com.fitflow.backend.dto.OkResponse;
import com.fitflow.backend.dto.PasswordChangeRequest;
import com.fitflow.backend.dto.UserInfoResponse;
import com.fitflow.backend.entity.AuthProvider;
import com.fitflow.backend.entity.User;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.UserRepository;
import com.fitflow.backend.security.JwtUtil;
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

    public AuthResponse signup(AuthSignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "Email already exists.");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .name("User")
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());
        return new AuthResponse(token, toUserInfo(saved));
    }

    public AuthResponse login(AuthLoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Invalid email or password."));

        if (user.getPassword() == null
                || user.getPassword().isBlank()
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
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
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        userRepository.save(updated);
        return new OkResponse(true);
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse("u_" + user.getId(), user.getEmail(), user.getName());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
