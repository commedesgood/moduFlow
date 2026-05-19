package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "월간 운동 날짜 목록 응답")
public class WorkoutDaysResponse {
    @Schema(description = "조회 월", example = "2026-05")
    private String month;

    @Schema(description = "운동 기록이 있는 날짜 목록", example = "[\"2026-05-01\", \"2026-05-14\"]")
    private List<String> days;
}
