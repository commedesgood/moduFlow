package com.moduflow.backend.service;

import com.moduflow.backend.dto.DeviceRegistrationRequest;
import com.moduflow.backend.dto.DeviceRegistrationResponse;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserDevice;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserDeviceRepository;
import com.moduflow.backend.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceRegistrationService {

    private static final int MAX_ANDROID_ID_LENGTH = 64;

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceRegistrationResponse register(Long userId, DeviceRegistrationRequest request) {
        return register(userId, request == null ? null : request.getAndroidId());
    }

    @Transactional
    public DeviceRegistrationResponse register(Long userId, String androidId) {
        User user = findActiveUser(userId);
        String normalizedAndroidId = normalizeAndroidId(androidId);

        UserDevice device = userDeviceRepository.findByAndroidId(normalizedAndroidId)
                .map(existing -> {
                    existing.assignTo(user.getId());
                    return existing;
                })
                .orElseGet(() -> new UserDevice(normalizedAndroidId, user.getId()));

        UserDevice saved = userDeviceRepository.save(device);
        Instant registeredAt = saved.getUpdatedAt() == null ? Instant.now() : saved.getUpdatedAt();
        return new DeviceRegistrationResponse(user.getId(), maskAndroidId(saved.getAndroidId()), registeredAt);
    }

    public String normalizeAndroidId(String androidId) {
        if (androidId == null || androidId.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "DEVICE_BAD_REQUEST", "androidId is required.");
        }

        String normalized = androidId.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_ANDROID_ID_LENGTH) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "DEVICE_BAD_REQUEST", "androidId must be 64 characters or less.");
        }
        return normalized;
    }

    public String maskAndroidId(String androidId) {
        if (androidId == null || androidId.isBlank()) {
            return "";
        }

        String normalized = androidId.trim();
        if (normalized.length() <= 4) {
            return "*".repeat(normalized.length());
        }
        if (normalized.length() <= 8) {
            return normalized.substring(0, 2) + "*".repeat(normalized.length() - 4) + normalized.substring(normalized.length() - 2);
        }
        return normalized.substring(0, 4) + "*".repeat(normalized.length() - 8) + normalized.substring(normalized.length() - 4);
    }

    private User findActiveUser(Long userId) {
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "User authentication is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "User was not found."));
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "USER_INACTIVE", "User is inactive.");
        }
        return user;
    }
}
