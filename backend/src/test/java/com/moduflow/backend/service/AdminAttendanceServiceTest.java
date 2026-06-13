package com.moduflow.backend.service;

import com.moduflow.backend.dto.AdminAttendancePageResponse;
import com.moduflow.backend.dto.AdminAttendanceStatus;
import com.moduflow.backend.entity.Attendance;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.repository.AttendanceRepository;
import com.moduflow.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAttendanceServiceTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-08T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Test
    void returnsAttendedAndAbsentMembersWithUnmaskedPrivateDataForAdmin() {
        AdminAttendanceService service = new AdminAttendanceService(userRepository, attendanceRepository, clock);
        when(userRepository.findAdminAttendanceUsers(
                eq(UserRole.USER),
                eq(LocalDateTime.of(2026, 6, 8, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 9, 0, 0)),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(
                        user(10L, "testuser@naver.com", "김모두"),
                        user(11L, "member@gmail.com", "이수")
                ),
                PageRequest.of(0, 20),
                2
        ));
        when(attendanceRepository.findFirstAttendancesForUsers(
                eq(List.of(10L, 11L)),
                eq(LocalDateTime.of(2026, 6, 8, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 9, 0, 0))
        )).thenReturn(List.of(attendance(10L, LocalDateTime.of(2026, 6, 8, 10, 0), "유산소 존")));

        AdminAttendancePageResponse response = service.getAttendances(
                LocalDate.of(2026, 6, 8),
                null,
                null,
                0,
                20
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).userId()).isEqualTo(10L);
        assertThat(response.content().get(0).maskedEmail()).isEqualTo("testuser@naver.com");
        assertThat(response.content().get(0).maskedName()).isEqualTo("김모두");
        assertThat(response.content().get(0).status()).isEqualTo(AdminAttendanceStatus.ATTENDED);
        assertThat(response.content().get(0).checkInAt().toString()).isEqualTo("2026-06-08T10:00+09:00");
        assertThat(response.content().get(0).zoneName()).isEqualTo("유산소 존");

        assertThat(response.content().get(1).userId()).isEqualTo(11L);
        assertThat(response.content().get(1).maskedEmail()).isEqualTo("member@gmail.com");
        assertThat(response.content().get(1).maskedName()).isEqualTo("이수");
        assertThat(response.content().get(1).status()).isEqualTo(AdminAttendanceStatus.ABSENT);
        assertThat(response.content().get(1).checkInAt()).isNull();
        assertThat(response.content().get(1).zoneName()).isNull();
    }

    @Test
    void returnsMemberOnlyOnceEvenWhenRepositoryReturnsDuplicateFirstAttendances() {
        AdminAttendanceService service = new AdminAttendanceService(userRepository, attendanceRepository, clock);
        when(userRepository.findAdminAttendanceUsers(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user(10L, "testuser@naver.com", "김모두"))));
        when(attendanceRepository.findFirstAttendancesForUsers(eq(List.of(10L)), any(), any()))
                .thenReturn(List.of(
                        attendance(10L, LocalDateTime.of(2026, 6, 8, 9, 0), "유산소 존"),
                        attendance(10L, LocalDateTime.of(2026, 6, 8, 9, 0), "웨이트 존")
                ));

        AdminAttendancePageResponse response = service.getAttendances(LocalDate.of(2026, 6, 8), null, null, 0, 20);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).status()).isEqualTo(AdminAttendanceStatus.ATTENDED);
        assertThat(response.content().get(0).zoneName()).isEqualTo("유산소 존");
    }

    @Test
    void appliesStatusKeywordAndPaginationInRepositoryQuery() {
        AdminAttendanceService service = new AdminAttendanceService(userRepository, attendanceRepository, clock);
        when(userRepository.findAdminAttendanceUsers(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 13));

        AdminAttendancePageResponse response = service.getAttendances(
                LocalDate.of(2026, 6, 8),
                AdminAttendanceStatus.ATTENDED,
                " Test ",
                2,
                5
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAdminAttendanceUsers(
                eq(UserRole.USER),
                eq(LocalDateTime.of(2026, 6, 8, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 9, 0, 0)),
                eq("ATTENDED"),
                eq("test"),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(13);
    }

    private UserRepository.AdminAttendanceUserProjection user(Long userId, String email, String name) {
        return new UserRepository.AdminAttendanceUserProjection() {
            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private Attendance attendance(Long userId, LocalDateTime checkInAt, String zoneName) {
        Attendance attendance = new Attendance(userId, "User", "ModuFlow");
        attendance.setCheckInTime(checkInAt);
        attendance.setZoneName(zoneName);
        return attendance;
    }
}
