package com.fitflow.backend.service;

import com.fitflow.backend.dto.OkResponse;
import com.fitflow.backend.dto.SettingsRequest;
import com.fitflow.backend.dto.SettingsResponse;
import com.fitflow.backend.entity.UserSettings;
import com.fitflow.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsV1Service {

    private final UserSettingsRepository userSettingsRepository;

    public SettingsResponse get(Long userId) {
        boolean enabled = userSettingsRepository.findByUserId(userId)
                .map(UserSettings::isAutoAttendanceEnabled)
                .orElse(false);
        return new SettingsResponse(enabled);
    }

    @Transactional
    public OkResponse save(Long userId, SettingsRequest request) {
        boolean enabled = request.getAutoAttendanceEnabled() != null && request.getAutoAttendanceEnabled();
        UserSettings entity = userSettingsRepository.findByUserId(userId)
                .map(existing -> UserSettings.builder()
                        .userId(existing.getUserId())
                        .autoAttendanceEnabled(enabled)
                        .createdAt(existing.getCreatedAt())
                        .updatedAt(existing.getUpdatedAt())
                        .build())
                .orElseGet(() -> UserSettings.builder()
                        .userId(userId)
                        .autoAttendanceEnabled(enabled)
                        .build());

        userSettingsRepository.save(entity);
        return new OkResponse(true);
    }
}
