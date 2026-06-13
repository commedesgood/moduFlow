package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Admin member attendance page")
public record AdminAttendancePageResponse(
        List<AdminAttendanceItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
