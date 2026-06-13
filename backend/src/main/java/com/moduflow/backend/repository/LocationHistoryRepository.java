package com.moduflow.backend.repository;

import com.moduflow.backend.entity.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
}
