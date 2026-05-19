package com.fitflow.backend.controller;

import com.fitflow.backend.dto.OkResponse;
import com.fitflow.backend.dto.RoutineItemDto;
import com.fitflow.backend.security.CustomUserDetails;
import com.fitflow.backend.service.RoutinesV1Service;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "루틴 설정", description = "요일별 운동 루틴 조회 및 저장 API")
@RequestMapping("/api/v1/routines")
public class RoutinesV1Controller {

    private final RoutinesV1Service routinesV1Service;

    @GetMapping
    @Operation(
            operationId = "getRoutines",
            summary = "요일별 루틴 전체 조회",
            description = "mon~sun 요일 키 기준으로 사용자의 루틴 목록을 조회합니다. 응답 key는 `mon`, `tue`, `wed`, `thu`, `fri`, `sat`, `sun`입니다."
    )
    public Map<String, List<RoutineItemDto>> get(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return routinesV1Service.getRoutines(userDetails.getUserId());
    }

    @PutMapping
    @Operation(
            operationId = "replaceRoutines",
            summary = "요일별 루틴 전체 저장",
            description = "프론트에서 전달한 요일별 루틴 전체 데이터를 기존 데이터와 교체 저장합니다. 지원 요일 key는 `mon`, `tue`, `wed`, `thu`, `fri`, `sat`, `sun`입니다."
    )
    public OkResponse save(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @Valid @RequestBody Map<String, List<@Valid RoutineItemDto>> schedule) {
        log.info("PUT /api/v1/routines called. userId={}, days={}", userDetails.getUserId(), schedule == null ? 0 : schedule.size());
        return routinesV1Service.saveRoutines(userDetails.getUserId(), schedule);
    }

    @PostMapping
    @Operation(
            operationId = "replaceRoutinesPost",
            summary = "요일별 루틴 전체 저장(POST 호환)",
            description = "PUT과 동일하게 요일별 루틴 전체 데이터를 교체 저장합니다. 신규 프론트 코드는 PUT 사용을 권장합니다."
    )
    public OkResponse savePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @Valid @RequestBody Map<String, List<@Valid RoutineItemDto>> schedule) {
        log.info("POST /api/v1/routines called. userId={}, days={}", userDetails.getUserId(), schedule == null ? 0 : schedule.size());
        return routinesV1Service.saveRoutines(userDetails.getUserId(), schedule);
    }
}
