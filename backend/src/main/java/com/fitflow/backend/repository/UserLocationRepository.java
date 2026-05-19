package com.fitflow.backend.repository;

import com.fitflow.backend.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLocationRepository extends JpaRepository<UserLocation, String> {
}
