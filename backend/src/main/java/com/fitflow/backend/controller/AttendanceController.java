package com.fitflow.backend.controller;

import com.fitflow.backend.dto.ApiResponse;
import com.fitflow.backend.dto.AttendanceRequest;
import com.fitflow.backend.dto.AttendanceResponse;
import com.fitflow.backend.security.CustomUserDetails;
import com.fitflow.backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/attendance", "/api/v1/attendance"})
@Tag(name = "출석", description = "JWT 로그인 사용자 기준 출석/퇴장 및 헬스장 혼잡도 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // 출석 저장
    @PostMapping
    @Operation(
            summary = "출석 체크인",
            description = "요청 바디의 userName은 무시하고 JWT의 로그인 사용자 정보로 출석합니다. 같은 사용자가 같은 gymName에 미퇴장 상태로 중복 입장할 수 없습니다."
    )
    public ApiResponse<AttendanceResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @Valid @RequestBody AttendanceRequest request) {

        AttendanceResponse result = attendanceService.save(userDetails.getUserId(), userDetails.getName(), userDetails.getEmail(), request);

        return new ApiResponse<>(200, "출석 성공", result);
    }

    // 조회
    @GetMapping
    @Operation(
            summary = "내 출석 기록 조회",
            description = "JWT 로그인 사용자의 출석 기록만 조회합니다. gymName을 전달하면 해당 헬스장 기록으로 필터링합니다."
    )
    public ApiResponse<List<AttendanceResponse>> getAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "헬스장 이름 필터", example = "Fit Gym")
            @RequestParam(required = false) String gymName) {

        return new ApiResponse<>(200, "조회 성공",
                attendanceService.getAll(userDetails.getUserId(), gymName));
    }

    // 퇴장
    @PatchMapping("/checkout/{id}")
    @Operation(
            summary = "퇴장 체크아웃",
            description = "JWT 로그인 사용자의 출석 기록만 퇴장 처리할 수 있습니다. 다른 사용자의 출석 id는 처리하지 않습니다."
    )
    public void checkOut(@AuthenticationPrincipal CustomUserDetails userDetails,
                         @Parameter(description = "출석 기록 id", example = "1")
                         @PathVariable Long id) {
        attendanceService.checkOut(userDetails.getUserId(), id);
    }

    // 혼잡도
    @GetMapping("/congestion")
    @Operation(summary = "현재 혼잡도 조회", description = "특정 gymName의 미퇴장 출석 수를 반환합니다.")
    public ApiResponse<Integer> getCongestion(
            @Parameter(description = "헬스장 이름", example = "Fit Gym")
            @RequestParam String gymName) {
        return new ApiResponse<>(200, "혼잡도 조회 성공",
                attendanceService.getCongestion(gymName));
    }

    // 최근 혼잡도
    @GetMapping("/congestion/recent")
    @Operation(summary = "최근 1시간 혼잡도 조회", description = "특정 gymName에서 최근 1시간 내 체크인한 출석 수를 반환합니다.")
    public int getRecentCongestion(
            @Parameter(description = "헬스장 이름", example = "Fit Gym")
            @RequestParam String gymName) {
        return attendanceService.getRecentCongestion(gymName);
    }

}
