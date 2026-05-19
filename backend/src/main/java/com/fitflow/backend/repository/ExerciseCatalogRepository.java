package com.fitflow.backend.repository;

import com.fitflow.backend.entity.ExerciseCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCatalogRepository extends JpaRepository<ExerciseCatalog, String> {
}
