package com.moduflow.backend.service;

import com.moduflow.backend.dto.BeaconZoneCreateRequest;
import com.moduflow.backend.dto.BeaconZoneResponse;
import com.moduflow.backend.dto.BeaconZoneUpdateRequest;
import com.moduflow.backend.entity.BeaconZone;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.BeaconZoneRepository;
import com.moduflow.backend.repository.UserLocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BeaconZoneService {

    private final BeaconZoneRepository beaconZoneRepository;
    private final UserLocationRepository userLocationRepository;

    public BeaconZoneService(BeaconZoneRepository beaconZoneRepository,
                             UserLocationRepository userLocationRepository) {
        this.beaconZoneRepository = beaconZoneRepository;
        this.userLocationRepository = userLocationRepository;
    }

    @Transactional(readOnly = true)
    public List<BeaconZoneResponse> getAll() {
        return beaconZoneRepository.findAllByOrderByBeaconIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BeaconZoneResponse create(BeaconZoneCreateRequest request) {
        String beaconId = normalizeBeaconId(request.getBeaconId());
        String zoneName = normalizeZoneName(request.getZoneName());
        int capacity = normalizeCapacity(request.getCapacity());

        if (beaconZoneRepository.existsById(beaconId)) {
            throw new CustomException(
                    HttpStatus.CONFLICT,
                    "BEACON_ZONE_ALREADY_EXISTS",
                    "Beacon zone already exists."
            );
        }

        BeaconZone saved = beaconZoneRepository.save(new BeaconZone(beaconId, zoneName, capacity));
        return toResponse(saved);
    }

    @Transactional
    public BeaconZoneResponse update(String beaconId, BeaconZoneUpdateRequest request) {
        String normalizedBeaconId = normalizeBeaconId(beaconId);
        BeaconZone beaconZone = beaconZoneRepository.findById(normalizedBeaconId)
                .orElseThrow(() -> notFound(normalizedBeaconId));

        beaconZone.update(normalizeZoneName(request.getZoneName()), normalizeCapacity(request.getCapacity()));
        return toResponse(beaconZone);
    }

    @Transactional
    public void delete(String beaconId) {
        String normalizedBeaconId = normalizeBeaconId(beaconId);
        userLocationRepository.deleteByBeaconId(normalizedBeaconId);
        if (beaconZoneRepository.existsById(normalizedBeaconId)) {
            beaconZoneRepository.deleteById(normalizedBeaconId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<BeaconZone> findByBeaconId(String beaconId) {
        if (beaconId == null || beaconId.isBlank()) {
            return Optional.empty();
        }
        return beaconZoneRepository.findById(beaconId.trim());
    }

    private BeaconZoneResponse toResponse(BeaconZone beaconZone) {
        return new BeaconZoneResponse(
                beaconZone.getBeaconId(),
                beaconZone.getZoneId(),
                beaconZone.getZoneName(),
                beaconZone.getCapacity()
        );
    }

    private String normalizeBeaconId(String beaconId) {
        if (beaconId == null || beaconId.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "BEACON_ZONE_BAD_REQUEST", "beaconId is required.");
        }
        String normalized = beaconId.trim();
        if (normalized.length() > 64) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "BEACON_ZONE_BAD_REQUEST",
                    "beaconId must be 64 characters or less."
            );
        }
        return normalized;
    }

    private String normalizeZoneName(String zoneName) {
        if (zoneName == null || zoneName.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "BEACON_ZONE_BAD_REQUEST", "zoneName is required.");
        }
        String normalized = zoneName.trim();
        if (normalized.length() > 100) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "BEACON_ZONE_BAD_REQUEST",
                    "zoneName must be 100 characters or less."
            );
        }
        return normalized;
    }

    private int normalizeCapacity(Integer capacity) {
        if (capacity == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "BEACON_ZONE_BAD_REQUEST", "capacity is required.");
        }
        if (capacity < 0) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "BEACON_ZONE_BAD_REQUEST",
                    "capacity must be 0 or greater."
            );
        }
        return capacity;
    }

    private CustomException notFound(String beaconId) {
        return new CustomException(
                HttpStatus.NOT_FOUND,
                "BEACON_ZONE_NOT_FOUND",
                "Beacon zone was not found: " + beaconId
        );
    }
}
