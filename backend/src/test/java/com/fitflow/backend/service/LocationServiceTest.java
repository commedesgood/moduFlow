package com.fitflow.backend.service;

import com.fitflow.backend.dto.CurrentLocationResponse;
import com.fitflow.backend.dto.LocationRequest;
import com.fitflow.backend.entity.LocationHistory;
import com.fitflow.backend.entity.UserLocation;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.LocationHistoryRepository;
import com.fitflow.backend.repository.UserLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private UserLocationRepository userLocationRepository;

    @Mock
    private LocationHistoryRepository locationHistoryRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    void updateLocationUpsertsCurrentLocationAndWritesHistory() {
        LocationRequest request = request(" a1b2c3d4e5f6g7h8 ", 53626);
        when(userLocationRepository.findById("a1b2c3d4e5f6g7h8")).thenReturn(Optional.empty());

        locationService.updateLocation(request);

        ArgumentCaptor<UserLocation> locationCaptor = ArgumentCaptor.forClass(UserLocation.class);
        ArgumentCaptor<LocationHistory> historyCaptor = ArgumentCaptor.forClass(LocationHistory.class);

        verify(userLocationRepository).save(locationCaptor.capture());
        verify(locationHistoryRepository).save(historyCaptor.capture());

        UserLocation savedLocation = locationCaptor.getValue();
        assertThat(savedLocation.getUserId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(savedLocation.getZoneId()).isEqualTo(53626);
        assertThat(savedLocation.getZoneName()).isEqualTo("비콘1");
        assertThat(savedLocation.getUpdatedAt()).isNotNull();

        LocationHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getUserId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(savedHistory.getZoneId()).isEqualTo(53626);
        assertThat(savedHistory.getZoneName()).isEqualTo("비콘1");
    }

    @Test
    void updateLocationRejectsUndefinedZoneId() {
        LocationRequest request = request("a1b2c3d4e5f6g7h8", 99999);

        assertThatThrownBy(() -> locationService.updateLocation(request))
                .isInstanceOf(CustomException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userLocationRepository, never()).save(any());
        verify(locationHistoryRepository, never()).save(any());
    }

    @Test
    void getCurrentLocationReturnsStoredLocation() {
        UserLocation userLocation = UserLocation.builder()
                .userId("a1b2c3d4e5f6g7h8")
                .zoneId(0)
                .zoneName("이탈")
                .build();
        userLocation.changeLocation(0, "이탈");
        when(userLocationRepository.findById("a1b2c3d4e5f6g7h8")).thenReturn(Optional.of(userLocation));

        CurrentLocationResponse response = locationService.getCurrentLocation(" a1b2c3d4e5f6g7h8 ");

        assertThat(response.userId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(response.zoneId()).isEqualTo(0);
        assertThat(response.zoneName()).isEqualTo("이탈");
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
        return request;
    }
}
