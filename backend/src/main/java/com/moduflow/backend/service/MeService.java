package com.moduflow.backend.service;

import com.moduflow.backend.dto.ProfileNameUpdateRequest;
import com.moduflow.backend.dto.MeProfileResponse;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeService {

    private static final int MAX_NAME_LENGTH = 100;

    private final UserRepository userRepository;

    public MeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MeProfileResponse getMe(Long userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    @Transactional
    public MeProfileResponse updateName(Long userId, ProfileNameUpdateRequest request) {
        String name = normalizeName(request == null ? null : request.getName());
        User user = findUser(userId);
        user.changeName(name);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "User authentication is required.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "User was not found."));
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "PROFILE_BAD_REQUEST", "name is required.");
        }

        String normalized = name.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "PROFILE_BAD_REQUEST", "name must be at most 100 characters.");
        }
        return normalized;
    }

    private MeProfileResponse toResponse(User user) {
        return new MeProfileResponse(user.getId(), user.getEmail(), user.getName());
    }
}
