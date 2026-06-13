package com.moduflow.backend.controller;

import com.moduflow.backend.dto.AdminAttendancePageResponse;
import com.moduflow.backend.dto.AdminAttendanceStatus;
import com.moduflow.backend.dto.AdminDashboardSummaryResponse;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.AdminAttendanceService;
import com.moduflow.backend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin attendance and dashboard APIs")
public class AdminController {

    private final AdminAttendanceService adminAttendanceService;
    private final AdminDashboardService adminDashboardService;

    public AdminController(AdminAttendanceService adminAttendanceService,
                           AdminDashboardService adminDashboardService) {
        this.adminAttendanceService = adminAttendanceService;
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/attendances")
    @Operation(
            summary = "Admin member attendance status",
            description = "Lists active regular members with ATTENDED/ABSENT status for the selected Asia/Seoul date."
    )
    public AdminAttendancePageResponse getAttendances(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Asia/Seoul local date. Defaults to today.", example = "2026-06-08")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Attendance status filter")
            @RequestParam(required = false) AdminAttendanceStatus status,
            @Parameter(description = "Search by member name or email")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin(userDetails);
        return adminAttendanceService.getAttendances(date, status, keyword, page, size);
    }

    @GetMapping("/dashboard/summary")
    @Operation(
            summary = "Admin dashboard summary",
            description = "Returns active regular member count and distinct attendance count for the selected Asia/Seoul date."
    )
    public AdminDashboardSummaryResponse getDashboardSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Asia/Seoul local date. Defaults to today.", example = "2026-06-08")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        requireAdmin(userDetails);
        return adminDashboardService.getSummary(date);
    }

    private void requireAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Admin authentication is required.");
        }

        boolean admin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!admin) {
            throw new CustomException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Admin authority is required.");
        }
    }
}
