package com.moduflow.backend.repository;

import com.moduflow.backend.entity.BeaconZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeaconZoneRepository extends JpaRepository<BeaconZone, String> {

    List<BeaconZone> findAllByOrderByBeaconIdAsc();
}
