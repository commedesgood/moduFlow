package com.fitflow.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "workout_items",
        indexes = @Index(name = "idx_workout_items_day_order", columnList = "workoutDayId, sortOrder")
)
public class WorkoutItem {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String workoutDayId;

    @Column(length = 100)
    private String exerciseId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String note;

    private Integer sets;

    private Integer reps;

    @Column(precision = 6, scale = 2)
    private BigDecimal weight;

    private Integer durationSeconds;

    private Integer accuracy;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
