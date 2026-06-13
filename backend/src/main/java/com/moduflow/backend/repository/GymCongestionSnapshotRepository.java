package com.moduflow.backend.repository;

import com.moduflow.backend.entity.GymCongestionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymCongestionSnapshotRepository extends JpaRepository<GymCongestionSnapshot, Long> {
}
