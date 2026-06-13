package com.moduflow.backend.repository;

import com.moduflow.backend.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
}
