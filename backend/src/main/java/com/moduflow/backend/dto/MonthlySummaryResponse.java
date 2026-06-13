package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "월간 운동 요약 응답")
public class MonthlySummaryResponse {
    @Schema(description = "조회 월", example = "2026-05")
    private String month;

    @Schema(description = "월간 운동일 비율", example = "42")
    private int attendanceRate;

    @Schema(description = "운동한 날짜 수", example = "13")
    private int workoutDays;

    @Schema(description = "해당 월 전체 일수", example = "31")
    private int totalDaysInMonth;
}
