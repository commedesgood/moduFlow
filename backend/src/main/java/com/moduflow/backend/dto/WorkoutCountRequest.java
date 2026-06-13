package com.moduflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "운동 날짜 카운트 증감 요청")
public class WorkoutCountRequest {
    @Min(value = -100, message = "delta는 -100 이상이어야 합니다.")
    @Max(value = 100, message = "delta는 100 이하여야 합니다.")
    @Schema(description = "증감값. 생략하면 +1로 처리됩니다.", example = "1", minimum = "-100", maximum = "100")
    private Integer delta;
}
