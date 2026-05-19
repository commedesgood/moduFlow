package com.fitflow.backend.service;

import com.fitflow.backend.dto.AttendanceRequest;
import com.fitflow.backend.dto.AttendanceResponse;
import com.fitflow.backend.entity.Attendance;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.AttendanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void saveUsesAuthenticatedUserAndIgnoresRequestUserName() {
        AttendanceRequest request = new AttendanceRequest();
        ReflectionTestUtils.setField(request, "userName", "spoofed-user");
        ReflectionTestUtils.setField(request, "gymName", " Fit Gym ");

        when(attendanceRepository.findByUserIdAndGymNameAndCheckOutTimeIsNull(7L, "Fit Gym"))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> {
                    Attendance attendance = invocation.getArgument(0);
                    attendance.setId(15L);
                    return attendance;
                });

        AttendanceResponse response = attendanceService.save(7L, "Real User", "real@example.com", request);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getUserName()).isEqualTo("Real User");
        assertThat(response.getGymName()).isEqualTo("Fit Gym");
        verify(attendanceRepository, never()).findByUserNameAndGymNameAndCheckOutTimeIsNull(any(), any());
    }

    @Test
    void getAllReturnsOnlyAuthenticatedUsersRecords() {
        Attendance attendance = new Attendance(7L, "Real User", "Fit Gym");
        attendance.setId(1L);
        when(attendanceRepository.findByUserIdOrderByCheckInTimeDesc(7L)).thenReturn(List.of(attendance));

        List<AttendanceResponse> responses = attendanceService.getAll(7L, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserName()).isEqualTo("Real User");
        verify(attendanceRepository).findByUserIdOrderByCheckInTimeDesc(7L);
        verify(attendanceRepository, never()).findAll();
    }

    @Test
    void checkOutRejectsOtherUsersAttendanceRecord() {
        when(attendanceRepository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(7L, 99L))
                .isInstanceOf(CustomException.class)
                .hasMessage("출석 기록이 없습니다.");
    }
}
