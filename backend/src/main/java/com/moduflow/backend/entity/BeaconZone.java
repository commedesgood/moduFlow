package com.moduflow.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "beacon_zones")
public class BeaconZone {

    @Id
    @Column(name = "beacon_id", length = 64)
    private String beaconId;

    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BeaconZone() {
    }

    public BeaconZone(String beaconId, String zoneName, Integer capacity) {
        this.beaconId = beaconId;
        this.zoneName = zoneName;
        this.capacity = capacity;
    }

    public void update(String zoneName, Integer capacity) {
        this.zoneName = zoneName;
        this.capacity = capacity;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getBeaconId() {
        return beaconId;
    }

    public String getZoneId() {
        return beaconId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
