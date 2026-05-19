package com.fitflow.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "gym_congestion_snapshots",
        indexes = @Index(name = "idx_gym_congestion_snapshots_gym_measured", columnList = "gymId, measuredAt")
)
public class GymCongestionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String gymId;

    @Column(nullable = false)
    private Instant measuredAt;

    private Integer headcount;

    private Integer congestionPercent;

    @Column(length = 30)
    private String source;

    @PrePersist
    void onCreate() {
        if (measuredAt == null) {
            measuredAt = Instant.now();
        }
    }
}
