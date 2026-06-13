package com.moduflow.backend.repository;

import com.moduflow.backend.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, String> {
}
