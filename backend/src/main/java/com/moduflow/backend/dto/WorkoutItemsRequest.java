package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "특정 날짜 운동 아이템 전체 저장 요청")
public class WorkoutItemsRequest {
    @NotNull(message = "items는 필수입니다.")
    @Schema(description = "저장할 운동 아이템 목록. 빈 배열이면 해당 날짜의 운동 아이템을 삭제합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid WorkoutItemDto> items;
}
