package com.moduflow.backend.controller;

import com.moduflow.backend.dto.AdminAttendanceItemResponse;
import com.moduflow.backend.dto.AdminAttendancePageResponse;
import com.moduflow.backend.dto.AdminAttendanceStatus;
import com.moduflow.backend.dto.AdminDashboardSummaryResponse;
import com.moduflow.backend.exception.GlobalExceptionHandler;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.AdminAttendanceService;
import com.moduflow.backend.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerMockMvcTest {

    @Mock
    private AdminAttendanceService adminAttendanceService;

    @Mock
    private AdminDashboardService adminDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminController(adminAttendanceService, adminDashboardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .build();
    }

    @Test
    void getAttendancesRequiresAdminAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/attendances"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void getAttendancesRejectsNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/attendances").header("X-Test-Role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void getAttendancesReturnsPageContract() throws Exception {
        when(adminAttendanceService.getAttendances(
                eq(LocalDate.of(2026, 6, 8)),
                eq(AdminAttendanceStatus.ATTENDED),
                eq("김"),
                eq(0),
                eq(20)
        )).thenReturn(new AdminAttendancePageResponse(
                List.of(new AdminAttendanceItemResponse(
                        10L,
                        "testuser@naver.com",
                        "김모두",
                        AdminAttendanceStatus.ATTENDED,
                        OffsetDateTime.parse("2026-06-08T10:00:00+09:00"),
                        "유산소 존"
                )),
                0,
                20,
                328,
                17
        ));

        mockMvc.perform(get("/api/v1/admin/attendances")
                        .header("X-Test-Role", "ADMIN")
                        .param("date", "2026-06-08")
                        .param("status", "ATTENDED")
                        .param("keyword", "김")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(10))
                .andExpect(jsonPath("$.content[0].maskedEmail").value("testuser@naver.com"))
                .andExpect(jsonPath("$.content[0].maskedName").value("김모두"))
                .andExpect(jsonPath("$.content[0].status").value("ATTENDED"))
                .andExpect(jsonPath("$.content[0].checkInAt").value("2026-06-08T10:00:00+09:00"))
                .andExpect(jsonPath("$.content[0].zoneName").value("유산소 존"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(328))
                .andExpect(jsonPath("$.totalPages").value(17));

        verify(adminAttendanceService).getAttendances(
                LocalDate.of(2026, 6, 8),
                AdminAttendanceStatus.ATTENDED,
                "김",
                0,
                20
        );
    }

    @Test
    void getDashboardSummaryReturnsAggregateContract() throws Exception {
        when(adminDashboardService.getSummary(LocalDate.of(2026, 6, 8)))
                .thenReturn(new AdminDashboardSummaryResponse(328, 124, 204, 37.8));

        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .header("X-Test-Role", "ADMIN")
                        .param("date", "2026-06-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMembers").value(328))
                .andExpect(jsonPath("$.checkedInCount").value(124))
                .andExpect(jsonPath("$.absentCount").value(204))
                .andExpect(jsonPath("$.attendanceRate").value(37.8));
    }

    private static class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(CustomUserDetails.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            String role = webRequest.getHeader("X-Test-Role");
            if (role == null || role.isBlank()) {
                return null;
            }

            String authority = "ADMIN".equals(role) ? "ROLE_ADMIN" : "ROLE_USER";
            return new CustomUserDetails(
                    1L,
                    "admin@example.com",
                    "",
                    "Admin",
                    List.of(new SimpleGrantedAuthority(authority))
            );
        }
    }
}
