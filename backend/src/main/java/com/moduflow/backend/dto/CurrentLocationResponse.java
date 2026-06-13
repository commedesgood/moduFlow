package com.moduflow.backend.dto;

import java.time.LocalDateTime;

public record CurrentLocationResponse(
        String userId,
        String gymName,
        String beaconId,
        Integer zoneId,
        String zoneName,
        LocalDateTime updatedAt
) {
}
