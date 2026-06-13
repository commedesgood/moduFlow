package com.moduflow.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "location_history",
        indexes = @Index(name = "idx_location_history_user_changed", columnList = "user_id, changed_at")
)
public class LocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "gym_name", length = 100)
    private String gymName;

    @Column(name = "beacon_id", length = 64)
    private String beaconId;

    @Column(name = "zone_id", nullable = false)
    private Integer zoneId;

    @Column(name = "zone_name", nullable = false, length = 32)
    private String zoneName;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    protected LocationHistory() {
    }

    public LocationHistory(String userId, String gymName, String beaconId, Integer zoneId, String zoneName) {
        this.userId = userId;
        this.gymName = gymName;
        this.beaconId = beaconId;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
    }

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getGymName() {
        return gymName;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public Integer getZoneId() {
        return zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }
}
