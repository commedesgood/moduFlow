package com.moduflow.backend.repository;

import com.moduflow.backend.entity.RoutineRestDay;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoutineRestDayRepository extends JpaRepository<RoutineRestDay, String> {
    List<RoutineRestDay> findByUserIdOrderByDayOfWeekAsc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from RoutineRestDay r where r.userId = :userId")
    void deleteAllByUserIdInBulk(@Param("userId") Long userId);
}
