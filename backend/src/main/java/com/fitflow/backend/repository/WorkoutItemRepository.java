package com.fitflow.backend.repository;

import com.fitflow.backend.entity.WorkoutItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkoutItemRepository extends JpaRepository<WorkoutItem, String> {
    List<WorkoutItem> findByWorkoutDayIdOrderBySortOrderAsc(String workoutDayId);
    List<WorkoutItem> findByWorkoutDayIdInOrderByWorkoutDayIdAscSortOrderAsc(Collection<String> workoutDayIds);
    Optional<WorkoutItem> findByWorkoutDayIdAndId(String workoutDayId, String id);
    boolean existsByWorkoutDayId(String workoutDayId);
    void deleteByWorkoutDayId(String workoutDayId);
}
