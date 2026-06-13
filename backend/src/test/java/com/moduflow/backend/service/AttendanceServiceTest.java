package com.moduflow.backend.service;

import com.moduflow.backend.dto.AttendanceRequest;
import com.moduflow.backend.dto.AttendanceResponse;
import com.moduflow.backend.dto.BeaconCongestionResponse;
import com.moduflow.backend.entity.Attendance;
import com.moduflow.backend.entity.BeaconZone;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.AttendanceRepository;
import com.moduflow.backend.repository.BeaconZoneRepository;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.repository.UserLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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

    @Mock
    private UserLocationRepository userLocationRepository;

    @Mock
    private BeaconZoneRepository beaconZoneRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void saveUsesAuthenticatedUserAndIgnoresRequestUserName() {
        AttendanceRequest request = new AttendanceRequest();
        ReflectionTestUtils.setField(request, "userName", "spoofed-user");
        ReflectionTestUtils.setField(request, "gymName", " Fit Gym ");

        when(attendanceRepository.findByUserIdAndGymNameAndCheckOutTimeIsNull(7L, "Fit Gym"))
                .thenReturn(Optional.empty());
        when(userRepository.findById(7L)).thenReturn(Optional.empty());
        when(userLocationRepository.findById("7")).thenReturn(Optional.empty());
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
    @Test
    void getRecentCongestionReturnsBeaconCountsAndLevels() {
        LocalDateTime before = LocalDateTime.now().minusMinutes(2);
        when(userLocationRepository.countCurrentUsersByGymNameGroupedByBeaconId(
                org.mockito.ArgumentMatchers.eq("ModuFlow"),
                any(LocalDateTime.class)
        ))
                .thenReturn(List.of(
                        beaconCount("53626", 3),
                        beaconCount("53630", 1)
                ));
        when(beaconZoneRepository.findAllByOrderByBeaconIdAsc())
                .thenReturn(List.of(
                        new BeaconZone("53626", "Cardio Zone", 6),
                        new BeaconZone("53630", "Weights Zone", 3),
                        new BeaconZone("56376", "Stretch Zone", 9)
                ));

        BeaconCongestionResponse response = attendanceService.getRecentCongestion(" ModuFlow ");
        LocalDateTime after = LocalDateTime.now().minusMinutes(2);

        assertThat(response.beacons()).hasSize(3);
        assertThat(response.beacons().get(0).beaconId()).isEqualTo("53626");
        assertThat(response.beacons().get(0).zoneId()).isEqualTo("53626");
        assertThat(response.beacons().get(0).zoneName()).isEqualTo("Cardio Zone");
        assertThat(response.beacons().get(0).currentCount()).isEqualTo(3);
        assertThat(response.beacons().get(0).capacity()).isEqualTo(6);
        assertThat(response.beacons().get(0).peopleCount()).isEqualTo(3);
        assertThat(response.beacons().get(0).level()).isEqualTo(1);
        assertThat(response.beacons().get(1).beaconId()).isEqualTo("53630");
        assertThat(response.beacons().get(1).zoneName()).isEqualTo("Weights Zone");
        assertThat(response.beacons().get(1).currentCount()).isEqualTo(1);
        assertThat(response.beacons().get(1).capacity()).isEqualTo(3);
        assertThat(response.beacons().get(1).peopleCount()).isEqualTo(1);
        assertThat(response.beacons().get(1).level()).isZero();
        assertThat(response.beacons().get(2).beaconId()).isEqualTo("56376");
        assertThat(response.beacons().get(2).zoneName()).isEqualTo("Stretch Zone");
        assertThat(response.beacons().get(2).currentCount()).isZero();
        assertThat(response.beacons().get(2).capacity()).isEqualTo(9);
        assertThat(response.beacons().get(2).peopleCount()).isZero();
        assertThat(response.beacons().get(2).level()).isZero();
        assertThat(response.updatedAt()).isNotNull();

        org.mockito.ArgumentCaptor<LocalDateTime> activeSinceCaptor = org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userLocationRepository).countCurrentUsersByGymNameGroupedByBeaconId(
                org.mockito.ArgumentMatchers.eq("ModuFlow"),
                activeSinceCaptor.capture()
        );
        assertThat(activeSinceCaptor.getValue()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }

    private UserLocationRepository.BeaconUserCount beaconCount(String beaconId, long peopleCount) {
        return new UserLocationRepository.BeaconUserCount() {
            @Override
            public String getBeaconId() {
                return beaconId;
            }

            @Override
            public long getPeopleCount() {
                return peopleCount;
            }
        };
    }
}
