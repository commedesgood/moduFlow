package com.moduflow.backend.dto;

import java.time.Instant;
import java.util.List;

public record BeaconCongestionResponse(
        List<BeaconCongestionItem> beacons,
        Instant updatedAt
) {
    public record BeaconCongestionItem(
            String beaconId,
            String zoneId,
            String zoneName,
            long currentCount,
            int capacity,
            long peopleCount,
            int level
    ) {
    }
}
