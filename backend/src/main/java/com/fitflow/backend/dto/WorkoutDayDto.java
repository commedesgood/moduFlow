package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "날짜별 운동기록")
public class WorkoutDayDto {
    @Schema(description = "운동 날짜", example = "2026-05-14")
    private String date;

    @Schema(description = "운동 아이템 목록")
    private List<WorkoutItemDto> items;

    @Schema(description = "해당 날짜 운동 카운트", example = "2")
    private Integer workoutCount;
}
