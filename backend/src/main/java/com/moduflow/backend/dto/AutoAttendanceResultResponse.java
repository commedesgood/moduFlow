package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Automatic attendance processing result")
public record AutoAttendanceResultResponse(
        @Schema(description = "Automatic attendance status", example = "CREATED")
        AutoAttendanceStatus status,

        @Schema(description = "Attendance id when created or already checked in", example = "123")
        Long attendanceId,

        @Schema(description = "Check-in time in Asia/Seoul when available", example = "2026-06-09T15:40:00+09:00")
        OffsetDateTime checkedInAt
) {
}
