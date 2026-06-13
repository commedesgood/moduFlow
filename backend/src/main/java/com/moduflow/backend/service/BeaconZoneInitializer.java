package com.moduflow.backend.service;

import com.moduflow.backend.entity.BeaconZone;
import com.moduflow.backend.repository.BeaconZoneRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(1)
public class BeaconZoneInitializer implements ApplicationRunner {

    private final BeaconZoneRepository beaconZoneRepository;

    public BeaconZoneInitializer(BeaconZoneRepository beaconZoneRepository) {
        this.beaconZoneRepository = beaconZoneRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (beaconZoneRepository.count() > 0) {
            return;
        }

        List<BeaconZone> defaults = BeaconZones.DEFAULT_ZONES.stream()
                .map(zone -> new BeaconZone(zone.beaconId(), zone.zoneName(), zone.capacity()))
                .toList();
        beaconZoneRepository.saveAll(defaults);
    }
}
