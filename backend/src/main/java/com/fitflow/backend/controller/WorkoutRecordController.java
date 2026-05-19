package com.fitflow.backend.controller;

import com.fitflow.backend.dto.ApiResponse;
import com.fitflow.backend.dto.WorkoutRecordRequest;
import com.fitflow.backend.dto.WorkoutRecordResponse;
import com.fitflow.backend.security.CustomUserDetails;
import com.fitflow.backend.service.WorkoutRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/records")
public class WorkoutRecordController {

    private final WorkoutRecordService workoutRecordService;

    @PostMapping
    public ApiResponse<WorkoutRecordResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @Valid @RequestBody WorkoutRecordRequest request) {
        WorkoutRecordResponse saved = workoutRecordService.save(userDetails.getUserId(), request);
        return new ApiResponse<>(200, "저장 성공", saved);
    }

    @GetMapping("/my")
    public ApiResponse<List<WorkoutRecordResponse>> myRecords(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ApiResponse<>(200, "조회 성공", workoutRecordService.getMyRecords(userDetails.getUserId()));
    }
}
