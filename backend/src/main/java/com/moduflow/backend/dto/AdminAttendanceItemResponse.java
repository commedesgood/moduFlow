package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Admin member attendance row")
public record AdminAttendanceItemResponse(
        Long userId,
        String maskedEmail,
        String maskedName,
        AdminAttendanceStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime checkInAt,
        String zoneName
) {
}
