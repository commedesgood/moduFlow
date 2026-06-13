package com.moduflow.backend.service;

import java.util.List;

final class BeaconZones {

    static final String DEFAULT_GYM_NAME = "ModuFlow";
    static final String EXIT_BEACON_ID = "0";
    static final String EXIT_ZONE_NAME = "\uC774\uD0C8";
    static final List<DefaultZone> DEFAULT_ZONES = List.of(
            new DefaultZone("53626", "\uBE44\uCF581", 30),
            new DefaultZone("53630", "\uBE44\uCF582", 30),
            new DefaultZone("56376", "\uBE44\uCF583", 30)
    );

    private BeaconZones() {
    }

    static String beaconIdOf(Integer zoneId) {
        return zoneId == null ? null : String.valueOf(zoneId);
    }

    static boolean isExitZone(Integer zoneId) {
        return zoneId != null && EXIT_BEACON_ID.equals(beaconIdOf(zoneId));
    }

    record DefaultZone(String beaconId, String zoneName, int capacity) {
    }
}
