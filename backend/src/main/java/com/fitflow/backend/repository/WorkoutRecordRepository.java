package com.fitflow.backend.repository;

import com.fitflow.backend.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {
    List<WorkoutRecord> findByUserId(Long userId);
}
