package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin dashboard summary")
public record AdminDashboardSummaryResponse(
        long totalMembers,
        long checkedInCount,
        long absentCount,
        double attendanceRate
) {
}
