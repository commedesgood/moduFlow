package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(
        description = "요일별 루틴과 휴식일 목록",
        example = """
                {
                  "mon": [
                    {
                      "id": "7f3a2c1e-8d8b-4d3a-9f21-9d2b1bde1234",
                      "name": "스쿼트",
                      "sets": 3,
                      "reps": 10,
                      "weight": 60,
                      "exerciseId": "squat"
                    }
                  ],
                  "tue": [],
                  "wed": [],
                  "thu": [],
                  "fri": [],
                  "sat": [],
                  "sun": [],
                  "restDays": ["thu", "sun"]
                }
                """
)
public class RoutineScheduleDto {

    @Valid
    @Schema(description = "월요일 루틴 목록")
    private List<@Valid RoutineItemDto> mon;

    @Valid
    @Schema(description = "화요일 루틴 목록")
    private List<@Valid RoutineItemDto> tue;

    @Valid
    @Schema(description = "수요일 루틴 목록")
    private List<@Valid RoutineItemDto> wed;

    @Valid
    @Schema(description = "목요일 루틴 목록")
    private List<@Valid RoutineItemDto> thu;

    @Valid
    @Schema(description = "금요일 루틴 목록")
    private List<@Valid RoutineItemDto> fri;

    @Valid
    @Schema(description = "토요일 루틴 목록")
    private List<@Valid RoutineItemDto> sat;

    @Valid
    @Schema(description = "일요일 루틴 목록")
    private List<@Valid RoutineItemDto> sun;

    @Schema(
            description = "휴식일 목록. 생략 가능하며 값은 mon/tue/wed/thu/fri/sat/sun만 허용됩니다.",
            example = "[\"thu\", \"sun\"]",
            nullable = true
    )
    private List<
            @Pattern(
                    regexp = "mon|tue|wed|thu|fri|sat|sun",
                    message = "restDays only allows mon/tue/wed/thu/fri/sat/sun."
            ) String> restDays;
}
