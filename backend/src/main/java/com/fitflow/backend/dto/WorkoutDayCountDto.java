package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "날짜별 운동 카운트")
public class WorkoutDayCountDto {
    @Schema(description = "운동 날짜", example = "2026-05-14")
    private String date;

    @Schema(description = "운동 카운트", example = "2")
    private int workoutCount;
}
