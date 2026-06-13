package com.moduflow.backend.controller;

import com.moduflow.backend.dto.AutoAttendanceResultResponse;
import com.moduflow.backend.dto.AutoAttendanceStatus;
import com.moduflow.backend.dto.LocationRequest;
import com.moduflow.backend.dto.LocationUpdateResponse;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.exception.GlobalExceptionHandler;
import com.moduflow.backend.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LocationControllerMockMvcTest {

    @Mock
    private LocationService locationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LocationController(locationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateLocationReturnsLocationAndAttendanceResult() throws Exception {
        when(locationService.updateLocation(any()))
                .thenReturn(new LocationUpdateResponse(
                        true,
                        new AutoAttendanceResultResponse(AutoAttendanceStatus.DEVICE_NOT_REGISTERED, null, null)
                ));

        mockMvc.perform(post("/api/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "a1b2c3d4e5f6g7h8",
                                  "zoneId": 53626
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationUpdated").value(true))
                .andExpect(jsonPath("$.attendance.status").value("DEVICE_NOT_REGISTERED"))
                .andExpect(jsonPath("$.attendance.attendanceId").doesNotExist())
                .andExpect(jsonPath("$.attendance.checkedInAt").doesNotExist());

        verify(locationService).updateLocation(any());
    }

    @Test
    void updateLocationSupportsVersionedPath() throws Exception {
        when(locationService.updateLocation(any()))
                .thenReturn(new LocationUpdateResponse(
                        true,
                        new AutoAttendanceResultResponse(AutoAttendanceStatus.AUTO_ATTENDANCE_DISABLED, null, null)
                ));

        mockMvc.perform(post("/api/v1/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "a1b2c3d4e5f6g7h8",
                                  "zoneId": 53626
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendance.status").value("AUTO_ATTENDANCE_DISABLED"));

        verify(locationService).updateLocation(any());
    }

    @Test
    void updateLocationAcceptsBeaconAliasFields() throws Exception {
        when(locationService.updateLocation(any()))
                .thenReturn(new LocationUpdateResponse(
                        true,
                        new AutoAttendanceResultResponse(AutoAttendanceStatus.ALREADY_CHECKED_IN, 12L, null)
                ));

        mockMvc.perform(post("/api/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "androidId": "a1b2c3d4e5f6g7h8",
                                  "beaconId": "53630",
                                  "zoneId": "53630",
                                  "gymName": "ModuFlow"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<LocationRequest> requestCaptor = ArgumentCaptor.forClass(LocationRequest.class);
        verify(locationService).updateLocation(requestCaptor.capture());

        LocationRequest request = requestCaptor.getValue();
        assertThat(request.getUserId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(request.getZoneId()).isEqualTo(53630);
        assertThat(request.getGymName()).isEqualTo("ModuFlow");
    }

    @Test
    void updateLocationReturnsBadRequestForInvalidInput() throws Exception {
        doThrow(new CustomException(HttpStatus.BAD_REQUEST, "LOCATION_BAD_REQUEST", "Undefined zoneId."))
                .when(locationService)
                .updateLocation(any());

        mockMvc.perform(post("/api/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "a1b2c3d4e5f6g7h8",
                                  "zoneId": 99999
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_BAD_REQUEST"));
    }
}
