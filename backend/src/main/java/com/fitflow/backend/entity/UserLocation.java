package com.fitflow.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_location")
public class UserLocation {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "zone_id", nullable = false)
    private Integer zoneId;

    @Column(name = "zone_name", nullable = false, length = 32)
    private String zoneName;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void changeLocation(Integer zoneId, String zoneName) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.updatedAt = LocalDateTime.now();
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
}
