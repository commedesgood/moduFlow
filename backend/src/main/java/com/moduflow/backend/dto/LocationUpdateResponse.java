package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Beacon location update and automatic attendance result")
public record LocationUpdateResponse(
        @Schema(description = "Whether the location was stored", example = "true")
        boolean locationUpdated,

        @Schema(description = "Automatic attendance result")
        AutoAttendanceResultResponse attendance
) {
}
