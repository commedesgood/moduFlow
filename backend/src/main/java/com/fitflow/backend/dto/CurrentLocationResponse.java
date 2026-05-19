package com.fitflow.backend.dto;

import java.time.LocalDateTime;

public record CurrentLocationResponse(
        String userId,
        Integer zoneId,
        String zoneName,
        LocalDateTime updatedAt
) {
}
