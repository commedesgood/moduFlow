package com.moduflow.backend.repository;

import com.moduflow.backend.entity.WorkoutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkoutItemRepository extends JpaRepository<WorkoutItem, String> {
    List<WorkoutItem> findByWorkoutDayIdOrderBySortOrderAsc(String workoutDayId);
    List<WorkoutItem> findByWorkoutDayIdInOrderByWorkoutDayIdAscSortOrderAsc(Collection<String> workoutDayIds);
    Optional<WorkoutItem> findByWorkoutDayIdAndId(String workoutDayId, String id);
    boolean existsByWorkoutDayId(String workoutDayId);
    void deleteByWorkoutDayId(String workoutDayId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = "update workout_items set exercise_id = replace(exercise_id, '-', '') " +
                    "where exercise_id is not null and exercise_id like '%-%'",
            nativeQuery = true
    )
    int normalizeExerciseIds();
}
