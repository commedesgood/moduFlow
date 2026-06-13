package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
@Schema(description = "운동 아이템 부분 수정 요청. null 필드는 기존 값을 유지합니다.")
public class WorkoutItemPatchRequest {
    @Min(value = 0, message = "sets는 0 이상이어야 합니다.")
    @Max(value = 999, message = "sets는 999 이하여야 합니다.")
    @Schema(description = "세트 수", example = "4", minimum = "0", maximum = "999")
    private Integer sets;

    @Min(value = 0, message = "reps는 0 이상이어야 합니다.")
    @Max(value = 999, message = "reps는 999 이하여야 합니다.")
    @Schema(description = "반복 횟수", example = "12", minimum = "0", maximum = "999")
    private Integer reps;

    @PositiveOrZero(message = "weight는 0 이상이어야 합니다.")
    @Schema(description = "중량 kg", example = "62.5", minimum = "0")
    private Double weight;
}
