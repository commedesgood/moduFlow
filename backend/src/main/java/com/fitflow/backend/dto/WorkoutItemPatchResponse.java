package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "운동 아이템 수정 응답")
public class WorkoutItemPatchResponse {
    @Schema(description = "운동 날짜", example = "2026-05-14")
    private String date;

    @Schema(description = "수정된 운동 아이템")
    private WorkoutItemDto item;
}
