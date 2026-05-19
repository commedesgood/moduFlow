package com.fitflow.backend.repository;

import com.fitflow.backend.entity.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, String> {
    List<WorkoutDay> findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(Long userId, LocalDate from, LocalDate to);
    List<WorkoutDay> findByUserIdAndWorkoutDateBetweenAndWorkoutCountGreaterThanOrderByWorkoutDateDesc(Long userId, LocalDate from, LocalDate to, int workoutCount);
    Optional<WorkoutDay> findByUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);
    List<WorkoutDay> findByUserIdAndWorkoutDateBetween(Long userId, LocalDate from, LocalDate to);
}
