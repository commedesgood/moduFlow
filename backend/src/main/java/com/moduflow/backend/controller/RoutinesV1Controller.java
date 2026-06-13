package com.moduflow.backend.controller;

import com.moduflow.backend.dto.OkResponse;
import com.moduflow.backend.dto.RoutineScheduleDto;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.RoutinesV1Service;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "루틴 설정", description = "요일별 루틴과 휴식일(restDays) 조회 및 저장 API")
@RequestMapping("/api/v1/routines")
public class RoutinesV1Controller {

    private static final String ROUTINE_SCHEDULE_WITH_REST_DAYS_EXAMPLE = """
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
              "fri": [
                {
                  "id": "1f51c0b7-0c8f-4f5c-980d-7348144d6f90",
                  "name": "벤치프레스",
                  "sets": 4,
                  "reps": 8,
                  "weight": 50,
                  "exerciseId": "benchpress"
                }
              ],
              "sat": [],
              "sun": [],
              "restDays": ["thu", "sun"]
            }
            """;

    private static final String ROUTINE_SCHEDULE_LEGACY_EXAMPLE = """
            {
              "mon": [
                {
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
              "sun": []
            }
            """;

    private final RoutinesV1Service routinesV1Service;

    @GetMapping
    @Operation(
            operationId = "getRoutines",
            summary = "루틴 설정 조회",
            description = "현재 사용자의 요일별 루틴과 휴식일(restDays)을 조회합니다. restDays는 출석률 계산 제외 및 달력 휴식일 표시에 사용할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoutineScheduleDto.class),
                            examples = @ExampleObject(
                                    name = "routineSchedule",
                                    summary = "restDays 포함 응답 예시",
                                    value = ROUTINE_SCHEDULE_WITH_REST_DAYS_EXAMPLE
                            )
                    )
            )
    })
    public RoutineScheduleDto get(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return routinesV1Service.getRoutines(userId);
    }

    @PutMapping
    @Operation(
            operationId = "replaceRoutines",
            summary = "루틴 설정 저장",
            description = "요일별 루틴 전체를 교체 저장합니다. restDays는 선택 필드이며 생략해도 기존 클라이언트와 호환됩니다. restDays 값은 mon/tue/wed/thu/fri/sat/sun만 허용됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "요일별 루틴과 선택적인 휴식일 목록",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoutineScheduleDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "withRestDays",
                                            summary = "restDays 포함 저장 예시",
                                            value = ROUTINE_SCHEDULE_WITH_REST_DAYS_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "legacyWithoutRestDays",
                                            summary = "기존 클라이언트 호환 예시",
                                            description = "restDays 없이 보내도 정상 동작합니다.",
                                            value = ROUTINE_SCHEDULE_LEGACY_EXAMPLE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청. 예: restDays에 허용되지 않은 요일 포함")
    })
    public OkResponse save(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @Valid @RequestBody RoutineScheduleDto schedule) {
        log.info("PUT /api/v1/routines called. userId={}, hasRestDays={}",
                userDetails.getUserId(),
                schedule != null && schedule.getRestDays() != null);
        return routinesV1Service.saveRoutines(userDetails.getUserId(), schedule);
    }

    @PostMapping
    @Operation(
            operationId = "replaceRoutinesPost",
            summary = "루틴 설정 저장 (POST 호환)",
            description = "PUT /api/v1/routines와 동일하게 동작하는 호환 엔드포인트입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "요일별 루틴과 선택적인 휴식일 목록",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoutineScheduleDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "withRestDays",
                                            summary = "restDays 포함 저장 예시",
                                            value = ROUTINE_SCHEDULE_WITH_REST_DAYS_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "legacyWithoutRestDays",
                                            summary = "기존 클라이언트 호환 예시",
                                            description = "restDays 없이 보내도 정상 동작합니다.",
                                            value = ROUTINE_SCHEDULE_LEGACY_EXAMPLE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청. 예: restDays에 허용되지 않은 요일 포함")
    })
    public OkResponse savePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @Valid @RequestBody RoutineScheduleDto schedule) {
        log.info("POST /api/v1/routines called. userId={}, hasRestDays={}",
                userDetails.getUserId(),
                schedule != null && schedule.getRestDays() != null);
        return routinesV1Service.saveRoutines(userDetails.getUserId(), schedule);
    }
}
