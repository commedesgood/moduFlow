package com.fitflow.backend.repository;

import com.fitflow.backend.entity.GymCongestionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymCongestionSnapshotRepository extends JpaRepository<GymCongestionSnapshot, Long> {
}
