package com.moduflow.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_location")
public class UserLocation {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "gym_name", length = 100)
    private String gymName;

    @Column(name = "beacon_id", length = 64)
    private String beaconId;

    @Column(name = "zone_id", nullable = false)
    private Integer zoneId;

    @Column(name = "zone_name", nullable = false, length = 32)
    private String zoneName;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserLocation() {
    }

    public UserLocation(String userId) {
        this.userId = userId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void changeLocation(Integer zoneId, String beaconId, String zoneName, String gymName) {
        this.zoneId = zoneId;
        this.beaconId = beaconId;
        this.zoneName = zoneName;
        this.gymName = gymName;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeLocation(Integer zoneId, String zoneName) {
        changeLocation(zoneId, String.valueOf(zoneId), zoneName, "ModuFlow");
    }

    @PrePersist
    void onCreate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private final UserLocation userLocation = new UserLocation();

        public Builder userId(String userId) {
            userLocation.userId = userId;
            return this;
        }

        public Builder gymName(String gymName) {
            userLocation.gymName = gymName;
            return this;
        }

        public Builder beaconId(String beaconId) {
            userLocation.beaconId = beaconId;
            return this;
        }

        public Builder zoneId(Integer zoneId) {
            userLocation.zoneId = zoneId;
            return this;
        }

        public Builder zoneName(String zoneName) {
            userLocation.zoneName = zoneName;
            return this;
        }

        public UserLocation build() {
            return userLocation;
        }
    }
}
