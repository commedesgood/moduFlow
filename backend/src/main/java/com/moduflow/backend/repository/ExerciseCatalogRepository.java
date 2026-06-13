package com.moduflow.backend.repository;

import com.moduflow.backend.entity.ExerciseCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCatalogRepository extends JpaRepository<ExerciseCatalog, String> {
}
