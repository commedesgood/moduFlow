package com.fitflow.backend.controller;

import com.fitflow.backend.dto.OkResponse;
import com.fitflow.backend.dto.WorkoutCountRequest;
import com.fitflow.backend.dto.WorkoutDayDto;
import com.fitflow.backend.dto.WorkoutDayCountDto;
import com.fitflow.backend.dto.WorkoutDayCountsResponse;
import com.fitflow.backend.dto.WorkoutItemPatchRequest;
import com.fitflow.backend.dto.WorkoutItemPatchResponse;
import com.fitflow.backend.dto.WorkoutItemsRequest;
import com.fitflow.backend.dto.WorkoutsResponse;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.security.CustomUserDetails;
import com.fitflow.backend.service.WorkoutsV1Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
@Tag(name = "운동기록", description = "날짜별 운동기록 조회, 저장, 수정, 삭제 API")
@RequestMapping("/api/v1/workouts")
public class WorkoutsV1Controller {

    private final WorkoutsV1Service workoutsV1Service;

    @GetMapping
    @Operation(
            operationId = "getWorkoutsBetween",
            summary = "기간 내 운동기록 조회",
            description = "from~to 기간의 날짜별 운동기록과 운동 아이템 목록을 조회합니다. from은 to보다 이후일 수 없습니다."
    )
    public WorkoutsResponse getBetween(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @Parameter(description = "조회 시작일", example = "2026-05-01")
                                       @RequestParam String from,
                                       @Parameter(description = "조회 종료일", example = "2026-05-31")
                                       @RequestParam String to) {
        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");
        validateDateRange(fromDate, toDate);
        return workoutsV1Service.getBetween(userDetails.getUserId(), fromDate, toDate);
    }

    // 달력용: 날짜별 운동횟수만 조회(아이템 리스트 제외)
    @GetMapping("/counts")
    @Operation(
            operationId = "getWorkoutCountsBetween",
            summary = "기간 내 운동 카운트 조회",
            description = "from~to 기간의 날짜별 workoutCount만 조회합니다. workoutCount가 0인 날짜는 제외됩니다."
    )
    public WorkoutDayCountsResponse getDayCountsBetween(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @Parameter(description = "조회 시작일", example = "2026-05-01")
                                                       @RequestParam String from,
                                                       @Parameter(description = "조회 종료일", example = "2026-05-31")
                                                       @RequestParam String to) {
        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");
        validateDateRange(fromDate, toDate);
        return workoutsV1Service.getDayCountsBetween(userDetails.getUserId(), fromDate, toDate);
    }

    @GetMapping("/{date}")
    @Operation(
            operationId = "getWorkoutDay",
            summary = "특정 날짜 운동기록 조회",
            description = "YYYY-MM-DD 날짜의 운동기록과 운동 아이템 목록을 조회합니다."
    )
    public WorkoutDayDto getOne(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Parameter(description = "조회 날짜", example = "2026-05-14")
                                @PathVariable String date) {
        return workoutsV1Service.getOne(userDetails.getUserId(), parseDate(date, "date"));
    }

    @PutMapping("/{date}")
    @Operation(
            operationId = "replaceWorkoutDay",
            summary = "특정 날짜 운동기록 저장",
            description = "YYYY-MM-DD 날짜의 운동기록 아이템 전체를 덮어쓰기 저장합니다. items가 빈 배열이면 해당 날짜의 운동 아이템을 삭제합니다."
    )
    public OkResponse putDay(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @Parameter(description = "저장 날짜", example = "2026-05-14")
                             @PathVariable String date,
                             @Valid @RequestBody WorkoutItemsRequest request) {
        return workoutsV1Service.putDay(userDetails.getUserId(), parseDate(date, "date"), request);
    }

    @PatchMapping("/{date}/items/{itemId}")
    @Operation(
            operationId = "patchWorkoutItem",
            summary = "운동기록 아이템 수정",
            description = "특정 날짜의 특정 운동 아이템 sets, reps, weight 값을 수정합니다. null 필드는 기존 값을 유지합니다."
    )
    public WorkoutItemPatchResponse patchItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @Parameter(description = "운동 날짜", example = "2026-05-14")
                                              @PathVariable String date,
                                              @Parameter(description = "운동 아이템 id", example = "7f3a2c1e-8d8b-4d3a-9f21-9d2b1bde1234")
                                              @PathVariable String itemId,
                                              @Valid @RequestBody WorkoutItemPatchRequest request) {
        return workoutsV1Service.patchItem(userDetails.getUserId(), parseDate(date, "date"), itemId, request);
    }

    @DeleteMapping("/{date}/items/{itemId}")
    @Operation(
            operationId = "deleteWorkoutItem",
            summary = "운동기록 아이템 삭제",
            description = "특정 날짜의 특정 운동 아이템을 삭제합니다."
    )
    public OkResponse deleteItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Parameter(description = "운동 날짜", example = "2026-05-14")
                                 @PathVariable String date,
                                 @Parameter(description = "운동 아이템 id", example = "7f3a2c1e-8d8b-4d3a-9f21-9d2b1bde1234")
                                 @PathVariable String itemId) {
        return workoutsV1Service.deleteItem(userDetails.getUserId(), parseDate(date, "date"), itemId);
    }

    // 달력용: 특정 날짜 운동횟수(누적) 증가/감소. 기본 delta=+1
    @PostMapping("/{date}/count")
    @Operation(
            operationId = "incrementWorkoutDayCount",
            summary = "운동 날짜 카운트 증감",
            description = "특정 날짜의 workoutCount를 delta 값만큼 증가 또는 감소시킵니다. request body를 생략하면 +1로 처리합니다."
    )
    public WorkoutDayCountDto incrementDayCount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @Parameter(description = "운동 날짜", example = "2026-05-14")
                                                @PathVariable String date,
                                                @Valid @RequestBody(required = false) WorkoutCountRequest request) {
        int delta = (request == null || request.getDelta() == null) ? 1 : request.getDelta();
        return workoutsV1Service.incrementDayCount(userDetails.getUserId(), parseDate(date, "date"), delta);
    }

    private LocalDate parseDate(String value, String field) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", field + "는 YYYY-MM-DD 형식이어야 합니다.");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "from은 to보다 이후일 수 없습니다.");
        }
    }
}
