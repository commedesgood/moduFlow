package com.moduflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BeaconZoneCreateRequest {

    @JsonAlias({"beacon_id", "zoneId", "zone_id"})
    @NotBlank(message = "beaconId is required.")
    @Size(max = 64, message = "beaconId must be 64 characters or less.")
    private String beaconId;

    @JsonAlias({"zone_name"})
    @NotBlank(message = "zoneName is required.")
    @Size(max = 100, message = "zoneName must be 100 characters or less.")
    private String zoneName;

    @NotNull(message = "capacity is required.")
    @Min(value = 0, message = "capacity must be 0 or greater.")
    private Integer capacity;

    public String getBeaconId() {
        return beaconId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
