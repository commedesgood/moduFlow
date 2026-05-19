package com.fitflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "요일별 루틴 아이템")
public class RoutineItemDto {
    @Size(max = 36, message = "id는 최대 36자까지 가능합니다.")
    @Schema(description = "루틴 아이템 id. 신규 저장 시 생략하면 서버가 UUID를 생성합니다.", example = "7f3a2c1e-8d8b-4d3a-9f21-9d2b1bde1234")
    private String id;

    @NotBlank(message = "name은 필수입니다.")
    @Size(max = 100, message = "name은 최대 100자까지 가능합니다.")
    @Schema(description = "운동 이름", example = "벤치프레스", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String name;

    @Min(value = 0, message = "sets는 0 이상이어야 합니다.")
    @Max(value = 999, message = "sets는 999 이하여야 합니다.")
    @Schema(description = "세트 수", example = "3", minimum = "0", maximum = "999")
    private Integer sets;

    @Min(value = 0, message = "reps는 0 이상이어야 합니다.")
    @Max(value = 999, message = "reps는 999 이하여야 합니다.")
    @Schema(description = "반복 횟수", example = "10", minimum = "0", maximum = "999")
    private Integer reps;

    @PositiveOrZero(message = "weight는 0 이상이어야 합니다.")
    @Schema(description = "중량 kg", example = "40.0", minimum = "0")
    private Double weight;

    @Size(max = 100, message = "exerciseId는 최대 100자까지 가능합니다.")
    @Schema(description = "운동 카탈로그 id 또는 프론트 운동 id", example = "bench-press")
    private String exerciseId;
}
