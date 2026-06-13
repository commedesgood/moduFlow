package com.moduflow.backend.service;

import com.moduflow.backend.dto.AdminDashboardSummaryResponse;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-08T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private UserRepository userRepository;

    @Test
    void returnsDashboardSummaryUsingAggregateCounts() {
        AdminDashboardService service = new AdminDashboardService(userRepository, clock);
        when(userRepository.countActiveRegularMembers(UserRole.USER)).thenReturn(328L);
        when(userRepository.countActiveRegularMembersAttendedBetween(
                UserRole.USER,
                LocalDateTime.of(2026, 6, 8, 0, 0),
                LocalDateTime.of(2026, 6, 9, 0, 0)
        )).thenReturn(124L);

        AdminDashboardSummaryResponse response = service.getSummary(LocalDate.of(2026, 6, 8));

        assertThat(response.totalMembers()).isEqualTo(328);
        assertThat(response.checkedInCount()).isEqualTo(124);
        assertThat(response.absentCount()).isEqualTo(204);
        assertThat(response.attendanceRate()).isEqualTo(37.8);
        verify(userRepository).countActiveRegularMembersAttendedBetween(
                UserRole.USER,
                LocalDateTime.of(2026, 6, 8, 0, 0),
                LocalDateTime.of(2026, 6, 9, 0, 0)
        );
    }

    @Test
    void attendanceRateIsZeroWhenTotalMembersIsZero() {
        AdminDashboardService service = new AdminDashboardService(userRepository, clock);
        when(userRepository.countActiveRegularMembers(UserRole.USER)).thenReturn(0L);
        when(userRepository.countActiveRegularMembersAttendedBetween(
                UserRole.USER,
                LocalDateTime.of(2026, 6, 8, 0, 0),
                LocalDateTime.of(2026, 6, 9, 0, 0)
        )).thenReturn(0L);

        AdminDashboardSummaryResponse response = service.getSummary(LocalDate.of(2026, 6, 8));

        assertThat(response.totalMembers()).isZero();
        assertThat(response.checkedInCount()).isZero();
        assertThat(response.absentCount()).isZero();
        assertThat(response.attendanceRate()).isZero();
    }
}
