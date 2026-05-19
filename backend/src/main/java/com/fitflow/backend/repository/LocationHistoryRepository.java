package com.fitflow.backend.repository;

import com.fitflow.backend.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
}
