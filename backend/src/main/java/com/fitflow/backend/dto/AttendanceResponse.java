package com.fitflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "출석 기록 응답")
public class AttendanceResponse {
    @Schema(description = "출석 기록 id", example = "1")
    private Long id;

    @Schema(description = "JWT 사용자 기준 표시 이름", example = "User")
    private String userName;

    @Schema(description = "헬스장 이름", example = "Fit Gym")
    private String gymName;

    @Schema(description = "체크인 시각", example = "2026-05-14T09:00:00")
    private LocalDateTime checkInTime;

    @Schema(description = "체크아웃 시각. 미퇴장 상태이면 null", example = "2026-05-14T10:30:00")
    private LocalDateTime checkOutTime;
}
