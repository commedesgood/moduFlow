package com.moduflow.backend.service;

import com.moduflow.backend.dto.BeaconZoneCreateRequest;
import com.moduflow.backend.dto.BeaconZoneResponse;
import com.moduflow.backend.dto.BeaconZoneUpdateRequest;
import com.moduflow.backend.entity.BeaconZone;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.BeaconZoneRepository;
import com.moduflow.backend.repository.UserLocationRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeaconZoneServiceTest {

    @Mock
    private BeaconZoneRepository beaconZoneRepository;

    @Mock
    private UserLocationRepository userLocationRepository;

    @InjectMocks
    private BeaconZoneService beaconZoneService;

    @Test
    void createStoresBeaconZoneSettings() {
        BeaconZoneCreateRequest request = createRequest(" 53626 ", " Cardio Zone ", 30);
        when(beaconZoneRepository.existsById("53626")).thenReturn(false);
        when(beaconZoneRepository.save(org.mockito.ArgumentMatchers.any(BeaconZone.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BeaconZoneResponse response = beaconZoneService.create(request);

        ArgumentCaptor<BeaconZone> captor = ArgumentCaptor.forClass(BeaconZone.class);
        verify(beaconZoneRepository).save(captor.capture());

        assertThat(captor.getValue().getBeaconId()).isEqualTo("53626");
        assertThat(captor.getValue().getZoneName()).isEqualTo("Cardio Zone");
        assertThat(captor.getValue().getCapacity()).isEqualTo(30);
        assertThat(response.beaconId()).isEqualTo("53626");
        assertThat(response.zoneId()).isEqualTo("53626");
        assertThat(response.zoneName()).isEqualTo("Cardio Zone");
        assertThat(response.capacity()).isEqualTo(30);
    }

    @Test
    void updateRejectsUnknownBeaconZone() {
        when(beaconZoneRepository.findById("53626")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beaconZoneService.update("53626", updateRequest("Cardio Zone", 30)))
                .isInstanceOf(CustomException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteRemovesExistingBeaconZone() {
        when(beaconZoneRepository.existsById("53626")).thenReturn(true);

        beaconZoneService.delete(" 53626 ");

        verify(userLocationRepository).deleteByBeaconId("53626");
        verify(beaconZoneRepository).deleteById("53626");
    }

    @Test
    void deleteCleansCurrentLocationsEvenWhenBeaconZoneIsAlreadyMissing() {
        when(beaconZoneRepository.existsById("123123")).thenReturn(false);

        beaconZoneService.delete("123123");

        verify(userLocationRepository).deleteByBeaconId("123123");
        verify(beaconZoneRepository, never()).deleteById("123123");
    }

    private BeaconZoneCreateRequest createRequest(String beaconId, String zoneName, Integer capacity) {
        BeaconZoneCreateRequest request = new BeaconZoneCreateRequest();
        ReflectionTestUtils.setField(request, "beaconId", beaconId);
        ReflectionTestUtils.setField(request, "zoneName", zoneName);
        ReflectionTestUtils.setField(request, "capacity", capacity);
        return request;
    }

    private BeaconZoneUpdateRequest updateRequest(String zoneName, Integer capacity) {
        BeaconZoneUpdateRequest request = new BeaconZoneUpdateRequest();
        ReflectionTestUtils.setField(request, "zoneName", zoneName);
        ReflectionTestUtils.setField(request, "capacity", capacity);
        return request;
    }
}
