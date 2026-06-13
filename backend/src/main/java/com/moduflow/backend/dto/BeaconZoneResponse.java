package com.moduflow.backend.dto;

public record BeaconZoneResponse(
        String beaconId,
        String zoneId,
        String zoneName,
        int capacity
) {
}
