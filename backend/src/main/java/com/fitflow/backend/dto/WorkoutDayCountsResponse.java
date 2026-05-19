package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "기간 내 운동 카운트 목록 응답")
public class WorkoutDayCountsResponse {
    @Schema(description = "날짜별 운동 카운트 목록")
    private List<WorkoutDayCountDto> counts;
}
