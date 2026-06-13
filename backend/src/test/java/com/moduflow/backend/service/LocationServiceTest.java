package com.moduflow.backend.service;

import com.moduflow.backend.dto.CurrentLocationResponse;
import com.moduflow.backend.dto.LocationUpdateResponse;
import com.moduflow.backend.dto.LocationRequest;
import com.moduflow.backend.dto.AutoAttendanceStatus;
import com.moduflow.backend.entity.Attendance;
import com.moduflow.backend.entity.BeaconZone;
import com.moduflow.backend.entity.LocationHistory;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserDevice;
import com.moduflow.backend.entity.UserSettings;
import com.moduflow.backend.entity.UserLocation;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.AttendanceRepository;
import com.moduflow.backend.repository.LocationHistoryRepository;
import com.moduflow.backend.repository.UserDeviceRepository;
import com.moduflow.backend.repository.UserLocationRepository;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.repository.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private UserLocationRepository userLocationRepository;

    @Mock
    private LocationHistoryRepository locationHistoryRepository;

    @Mock
    private BeaconZoneService beaconZoneService;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    void updateLocationUpsertsCurrentLocationAndWritesHistory() {
        LocationRequest request = request(" a1b2c3d4e5f6g7h8 ", 53626);
        when(userLocationRepository.findById("a1b2c3d4e5f6g7h8")).thenReturn(Optional.empty());
        when(beaconZoneService.findByBeaconId("53626"))
                .thenReturn(Optional.of(new BeaconZone("53626", "Cardio Zone", 30)));
        when(userDeviceRepository.findByAndroidIdForUpdate("a1b2c3d4e5f6g7h8")).thenReturn(Optional.empty());

        LocationUpdateResponse response = locationService.updateLocation(request);

        ArgumentCaptor<UserLocation> locationCaptor = ArgumentCaptor.forClass(UserLocation.class);
        ArgumentCaptor<LocationHistory> historyCaptor = ArgumentCaptor.forClass(LocationHistory.class);

        verify(userLocationRepository).save(locationCaptor.capture());
        verify(locationHistoryRepository).save(historyCaptor.capture());

        UserLocation savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getUserId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(savedLocation.getGymName()).isEqualTo("ModuFlow");
        assertThat(savedLocation.getBeaconId()).isEqualTo("53626");
        assertThat(savedLocation.getZoneId()).isEqualTo(53626);
        assertThat(savedLocation.getZoneName()).isEqualTo("Cardio Zone");
        assertThat(savedLocation.getUpdatedAt()).isNotNull();

        LocationHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getUserId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(savedHistory.getGymName()).isEqualTo("ModuFlow");
        assertThat(savedHistory.getBeaconId()).isEqualTo("53626");
        assertThat(savedHistory.getZoneId()).isEqualTo(53626);
        assertThat(savedHistory.getZoneName()).isEqualTo("Cardio Zone");

        assertThat(response.locationUpdated()).isTrue();
        assertThat(response.attendance().status()).isEqualTo(AutoAttendanceStatus.DEVICE_NOT_REGISTERED);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void updateLocationRejectsUndefinedZoneId() {
        LocationRequest request = request("a1b2c3d4e5f6g7h8", 99999);
        when(beaconZoneService.findByBeaconId("99999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updateLocation(request))
                .isInstanceOf(CustomException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userLocationRepository, never()).save(any());
        verify(locationHistoryRepository, never()).save(any());
    }

    @Test
    void transientExitDoesNotOverwriteRecentIndoorLocation() {
        LocationRequest request = request("android-abc", 0);
        UserLocation currentLocation = new UserLocation("android-abc");
        currentLocation.changeLocation(53626, "53626", "Cardio Zone", "ModuFlow");
        when(userLocationRepository.findById("android-abc")).thenReturn(Optional.of(currentLocation));

        LocationUpdateResponse response = locationService.updateLocation(request);

        assertThat(response.locationUpdated()).isFalse();
        assertThat(response.attendance().status()).isEqualTo(AutoAttendanceStatus.NO_ACTION);
        assertThat(currentLocation.getZoneId()).isEqualTo(53626);
        assertThat(currentLocation.getBeaconId()).isEqualTo("53626");
        assertThat(currentLocation.getZoneName()).isEqualTo("Cardio Zone");

        ArgumentCaptor<LocationHistory> historyCaptor = ArgumentCaptor.forClass(LocationHistory.class);
        verify(userLocationRepository, never()).save(any());
        verify(locationHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getZoneId()).isZero();
        assertThat(historyCaptor.getValue().getBeaconId()).isEqualTo("0");
        assertThat(historyCaptor.getValue().getZoneName()).isEqualTo(BeaconZones.EXIT_ZONE_NAME);
        verify(userDeviceRepository, never()).findByAndroidIdForUpdate(any());
    }

    @Test
    void exitOverwritesIndoorLocationAfterGracePeriod() {
        LocationRequest request = request("android-abc", 0);
        UserLocation currentLocation = new UserLocation("android-abc");
        currentLocation.changeLocation(53626, "53626", "Cardio Zone", "ModuFlow");
        ReflectionTestUtils.setField(currentLocation, "updatedAt", LocalDateTime.now().minusSeconds(31));
        when(userLocationRepository.findById("android-abc")).thenReturn(Optional.of(currentLocation));

        LocationUpdateResponse response = locationService.updateLocation(request);

        assertThat(response.locationUpdated()).isTrue();
        assertThat(response.attendance().status()).isEqualTo(AutoAttendanceStatus.NO_ACTION);
        assertThat(currentLocation.getZoneId()).isZero();
        assertThat(currentLocation.getBeaconId()).isEqualTo("0");
        assertThat(currentLocation.getZoneName()).isEqualTo(BeaconZones.EXIT_ZONE_NAME);
        verify(userLocationRepository).save(currentLocation);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void updateLocationCreatesAttendanceWhenRegisteredDeviceHasAutoAttendanceEnabled() {
        LocationRequest request = request(" Android-ABC ", 53626);
        when(userLocationRepository.findById("android-abc")).thenReturn(Optional.empty());
        when(beaconZoneService.findByBeaconId("53626"))
                .thenReturn(Optional.of(new BeaconZone("53626", "Cardio Zone", 30)));
        when(userDeviceRepository.findByAndroidIdForUpdate("android-abc"))
                .thenReturn(Optional.of(new UserDevice("android-abc", 7L)));
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(user(7L)));
        when(userSettingsRepository.findByUserId(7L))
                .thenReturn(Optional.of(UserSettings.builder()
                        .userId(7L)
                        .autoAttendanceEnabled(true)
                        .build()));
        when(attendanceRepository.findFirstByUserIdAndGymNameAndCheckOutTimeIsNullAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThanOrderByCheckInTimeAsc(
                eq(7L),
                eq("ModuFlow"),
                any(),
                any()
        )).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> {
                    Attendance attendance = invocation.getArgument(0);
                    attendance.setId(123L);
                    return attendance;
                });

        LocationUpdateResponse response = locationService.updateLocation(request);

        assertThat(response.locationUpdated()).isTrue();
        assertThat(response.attendance().status()).isEqualTo(AutoAttendanceStatus.CREATED);
        assertThat(response.attendance().attendanceId()).isEqualTo(123L);
        assertThat(response.attendance().checkedInAt()).isNotNull();
        assertThat(response.attendance().checkedInAt().getOffset().getId()).isEqualTo("+09:00");

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(captor.getValue().getGymName()).isEqualTo("ModuFlow");
        assertThat(captor.getValue().getZoneName()).isEqualTo("Cardio Zone");
    }

    @Test
    void updateLocationReturnsAlreadyCheckedInWithoutCreatingDuplicateAttendance() {
        LocationRequest request = request("android-abc", 53626);
        Attendance existing = new Attendance(7L, "Member", "ModuFlow");
        existing.setId(55L);

        when(userLocationRepository.findById("android-abc")).thenReturn(Optional.empty());
        when(beaconZoneService.findByBeaconId("53626"))
                .thenReturn(Optional.of(new BeaconZone("53626", "Cardio Zone", 30)));
        when(userDeviceRepository.findByAndroidIdForUpdate("android-abc"))
                .thenReturn(Optional.of(new UserDevice("android-abc", 7L)));
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(user(7L)));
        when(userSettingsRepository.findByUserId(7L))
                .thenReturn(Optional.of(UserSettings.builder()
                        .userId(7L)
                        .autoAttendanceEnabled(true)
                        .build()));
        when(attendanceRepository.findFirstByUserIdAndGymNameAndCheckOutTimeIsNullAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThanOrderByCheckInTimeAsc(
                eq(7L),
                eq("ModuFlow"),
                any(),
                any()
        )).thenReturn(Optional.of(existing));

        LocationUpdateResponse response = locationService.updateLocation(request);

        assertThat(response.attendance().status()).isEqualTo(AutoAttendanceStatus.ALREADY_CHECKED_IN);
        assertThat(response.attendance().attendanceId()).isEqualTo(55L);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void updateLocationReturnsAutoAttendanceDisabledWhenSettingIsOff() {
        LocationRequest request = request("android-abc", 53626);
        when(userLocationRepository.findById("android-abc")).thenReturn(Optional.empty());
        when(beaconZoneService.findByBeaconId("53626"))
                .thenReturn(Optional.of(new BeaconZone("53626", "Cardio Zone", 30)));
        when(userDeviceRepository.findByAndroidIdForUpdate("android-abc"))
                .thenReturn(Optional.of(new UserDevice("android-abc", 7L)));
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(user(7L)));
        when(userSettingsRepository.findByUserId(7L))
                .thenReturn(Optional.of(UserSettings.builder()
                        .userId(7L)
                        .autoAttendanceEnabled(false)
                        .build()));

        LocationUpdateResponse response = locationService.updateLocation(request);

        assertThat(response.attendance().status()).isEqualTo(AutoAttendanceStatus.AUTO_ATTENDANCE_DISABLED);
        assertThat(response.attendance().attendanceId()).isNull();
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void getCurrentLocationReturnsStoredLocation() {
        UserLocation userLocation = UserLocation.builder()
                .userId("a1b2c3d4e5f6g7h8")
                .zoneId(0)
                .zoneName(BeaconZones.EXIT_ZONE_NAME)
                .build();
        userLocation.changeLocation(0, BeaconZones.EXIT_ZONE_NAME);
        when(userLocationRepository.findById("a1b2c3d4e5f6g7h8")).thenReturn(Optional.of(userLocation));

        CurrentLocationResponse response = locationService.getCurrentLocation(" a1b2c3d4e5f6g7h8 ");

        assertThat(response.userId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(response.gymName()).isEqualTo("ModuFlow");
        assertThat(response.beaconId()).isEqualTo("0");
        assertThat(response.zoneId()).isEqualTo(0);
        assertThat(response.zoneName()).isEqualTo(BeaconZones.EXIT_ZONE_NAME);
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void getCurrentLocationReturnsNotFoundForUnknownUser() {
        when(userLocationRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getCurrentLocation("unknown"))
                .isInstanceOf(CustomException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private LocationRequest request(String userId, Integer zoneId) {
        LocationRequest request = new LocationRequest();
        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "zoneId", zoneId);
        ReflectionTestUtils.setField(request, "gymName", " ModuFlow ");
        return request;
    }

    private User user(Long userId) {
        return User.builder()
                .id(userId)
                .email("member@example.com")
                .password("encoded-password")
                .name("Member")
                .active(true)
                .build();
    }
}
