package com.moduflow.backend.controller;

import com.moduflow.backend.dto.BeaconZoneCreateRequest;
import com.moduflow.backend.dto.BeaconZoneResponse;
import com.moduflow.backend.dto.BeaconZoneUpdateRequest;
import com.moduflow.backend.exception.GlobalExceptionHandler;
import com.moduflow.backend.service.BeaconZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BeaconZoneControllerMockMvcTest {

    @Mock
    private BeaconZoneService beaconZoneService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BeaconZoneController(beaconZoneService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllReturnsBeaconZones() throws Exception {
        when(beaconZoneService.getAll())
                .thenReturn(List.of(new BeaconZoneResponse("53626", "53626", "Cardio Zone", 30)));

        mockMvc.perform(get("/api/v1/beacon-zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].beaconId").value("53626"))
                .andExpect(jsonPath("$[0].zoneId").value("53626"))
                .andExpect(jsonPath("$[0].zoneName").value("Cardio Zone"))
                .andExpect(jsonPath("$[0].capacity").value(30));
    }

    @Test
    void createReturnsCreatedBeaconZone() throws Exception {
        when(beaconZoneService.create(any(BeaconZoneCreateRequest.class)))
                .thenReturn(new BeaconZoneResponse("53626", "53626", "Cardio Zone", 30));

        mockMvc.perform(post("/api/v1/beacon-zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "beaconId": "53626",
                                  "zoneName": "Cardio Zone",
                                  "capacity": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.beaconId").value("53626"))
                .andExpect(jsonPath("$.zoneName").value("Cardio Zone"))
                .andExpect(jsonPath("$.capacity").value(30));
    }

    @Test
    void updateUsesPathBeaconId() throws Exception {
        when(beaconZoneService.update(eq("53626"), any(BeaconZoneUpdateRequest.class)))
                .thenReturn(new BeaconZoneResponse("53626", "53626", "Cardio Zone", 40));

        mockMvc.perform(put("/api/v1/beacon-zones/53626")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "zoneName": "Cardio Zone",
                                  "capacity": 40
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(40));

        verify(beaconZoneService).update(eq("53626"), any(BeaconZoneUpdateRequest.class));
    }

    @Test
    void deleteReturnsOkResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/beacon-zones/53626"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(beaconZoneService).delete("53626");
    }

    @Test
    void deleteSupportsAdminCmsPath() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/beacon-zones/53626"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(beaconZoneService).delete("53626");
    }
}
