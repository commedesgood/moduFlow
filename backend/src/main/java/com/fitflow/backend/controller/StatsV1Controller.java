package com.fitflow.backend.controller;

import com.fitflow.backend.dto.MonthlySummaryResponse;
import com.fitflow.backend.dto.WorkoutDaysResponse;
import com.fitflow.backend.security.CustomUserDetails;
import com.fitflow.backend.service.StatsV1Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "통계", description = "월간 운동 요약 및 운동 날짜 조회 API")
@RequestMapping("/api/v1/stats")
public class StatsV1Controller {

    private final StatsV1Service statsV1Service;

    @GetMapping("/monthly-summary")
    @Operation(summary = "월간 운동 요약 조회", description = "YYYY-MM 기준 월간 운동일 수, 전체 일수, 출석률을 조회합니다.")
    public MonthlySummaryResponse monthlySummary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestParam String month) {
        return statsV1Service.monthlySummary(userDetails.getUserId(), month);
    }

    @GetMapping("/workout-days")
    @Operation(summary = "월간 운동 날짜 조회", description = "YYYY-MM 기준 운동기록이 있는 날짜 목록을 조회합니다.")
    public WorkoutDaysResponse workoutDays(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestParam String month) {
        return statsV1Service.workoutDays(userDetails.getUserId(), month);
    }
}
