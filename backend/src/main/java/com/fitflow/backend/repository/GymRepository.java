package com.fitflow.backend.repository;

import com.fitflow.backend.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, String> {
}
