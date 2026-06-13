package com.moduflow.backend.service;

import com.moduflow.backend.entity.UserLocation;
import com.moduflow.backend.repository.BeaconZoneRepository;
import com.moduflow.backend.repository.UserLocationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(2)
public class BeaconZoneCurrentLocationCleanupRunner implements ApplicationRunner {

    private final BeaconZoneRepository beaconZoneRepository;
    private final UserLocationRepository userLocationRepository;

    public BeaconZoneCurrentLocationCleanupRunner(BeaconZoneRepository beaconZoneRepository,
                                                 UserLocationRepository userLocationRepository) {
        this.beaconZoneRepository = beaconZoneRepository;
        this.userLocationRepository = userLocationRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> registeredBeaconIds = beaconZoneRepository.findAll().stream()
                .map(zone -> zone.getBeaconId())
                .collect(Collectors.toSet());
        if (registeredBeaconIds.isEmpty()) {
            return;
        }

        List<UserLocation> staleLocations = userLocationRepository.findAll().stream()
                .filter(location -> location.getBeaconId() != null)
                .filter(location -> !registeredBeaconIds.contains(location.getBeaconId()))
                .toList();

        if (!staleLocations.isEmpty()) {
            userLocationRepository.deleteAll(staleLocations);
        }
    }
}
