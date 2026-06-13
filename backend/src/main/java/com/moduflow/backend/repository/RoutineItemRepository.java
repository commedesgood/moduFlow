package com.moduflow.backend.repository;

import com.moduflow.backend.entity.RoutineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoutineItemRepository extends JpaRepository<RoutineItem, String> {
    List<RoutineItem> findByUserIdOrderByDayOfWeekAscSortOrderAsc(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RoutineItem r where r.userId = :userId")
    void deleteAllByUserIdInBulk(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = "update routine_items set exercise_id = replace(exercise_id, '-', '') " +
                    "where exercise_id is not null and exercise_id like '%-%'",
            nativeQuery = true
    )
    int normalizeExerciseIds();
}
