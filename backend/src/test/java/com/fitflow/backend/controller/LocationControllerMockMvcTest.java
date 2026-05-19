package com.fitflow.backend.controller;

import com.fitflow.backend.dto.LocationRequest;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.exception.GlobalExceptionHandler;
import com.fitflow.backend.service.LocationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    void updateLocationReturnsOkWithoutBody() throws Exception {
        mockMvc.perform(post("/api/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "a1b2c3d4e5f6g7h8",
                                  "zoneId": 53626
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(locationService).updateLocation(any());
    }

    @Test
    void updateLocationAcceptsBeaconAliasFields() throws Exception {
        mockMvc.perform(post("/api/update-location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "androidId": "a1b2c3d4e5f6g7h8",
                                  "minor": 53630
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<LocationRequest> requestCaptor = ArgumentCaptor.forClass(LocationRequest.class);
        verify(locationService).updateLocation(requestCaptor.capture());

        LocationRequest request = requestCaptor.getValue();
        assertThat(request.getUserId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(request.getZoneId()).isEqualTo(53630);
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
