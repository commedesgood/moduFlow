package com.fitflow.backend.service;

import com.fitflow.backend.dto.CurrentLocationResponse;
import com.fitflow.backend.dto.LocationRequest;
import com.fitflow.backend.entity.LocationHistory;
import com.fitflow.backend.entity.UserLocation;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.LocationHistoryRepository;
import com.fitflow.backend.repository.UserLocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private static final int MAX_USER_ID_LENGTH = 64;

    private final UserLocationRepository userLocationRepository;
    private final LocationHistoryRepository locationHistoryRepository;

    public LocationService(UserLocationRepository userLocationRepository,
                           LocationHistoryRepository locationHistoryRepository) {
        this.userLocationRepository = userLocationRepository;
        this.locationHistoryRepository = locationHistoryRepository;
    }

    @Transactional
    public void updateLocation(LocationRequest request) {
        if (request == null) {
            throw badRequest("Request body is required.");
        }

        String userId = normalizeUserId(request.getUserId());
        Integer zoneId = request.getZoneId();
        String zoneName = zoneNameOf(zoneId);

        UserLocation userLocation = userLocationRepository.findById(userId)
                .orElseGet(() -> UserLocation.builder()
                        .userId(userId)
                        .build());
        userLocation.changeLocation(zoneId, zoneName);

        userLocationRepository.save(userLocation);
        locationHistoryRepository.save(LocationHistory.builder()
                .userId(userId)
                .zoneId(zoneId)
                .zoneName(zoneName)
                .build());
    }

    @Transactional(readOnly = true)
    public CurrentLocationResponse getCurrentLocation(String userId) {
        String normalizedUserId = normalizeUserId(userId);

        UserLocation userLocation = userLocationRepository.findById(normalizedUserId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "LOCATION_NOT_FOUND",
                        "Current location was not found."
                ));

        return new CurrentLocationResponse(
                userLocation.getUserId(),
                userLocation.getZoneId(),
                userLocation.getZoneName(),
                userLocation.getUpdatedAt()
        );
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw badRequest("userId is required.");
        }

        String normalized = userId.trim();
        if (normalized.length() > MAX_USER_ID_LENGTH) {
            throw badRequest("userId must be 64 characters or less.");
        }

        return normalized;
    }

    private String zoneNameOf(Integer zoneId) {
        if (zoneId == null) {
            throw badRequest("zoneId is required.");
        }

        return switch (zoneId) {
            case 53626 -> "비콘1";
            case 53630 -> "비콘2";
            case 56376 -> "비콘3";
            case 0 -> "이탈";
            default -> throw badRequest("Undefined zoneId.");
        };
    }

    private CustomException badRequest(String message) {
        return new CustomException(HttpStatus.BAD_REQUEST, "LOCATION_BAD_REQUEST", message);
    }
}
