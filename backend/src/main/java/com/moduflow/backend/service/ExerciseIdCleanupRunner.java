package com.moduflow.backend.service;

import com.moduflow.backend.repository.RoutineItemRepository;
import com.moduflow.backend.repository.WorkoutItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseIdCleanupRunner implements ApplicationRunner {

    private final RoutineItemRepository routineItemRepository;
    private final WorkoutItemRepository workoutItemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updatedRoutineItems = routineItemRepository.normalizeExerciseIds();
        int updatedWorkoutItems = workoutItemRepository.normalizeExerciseIds();

        if (updatedRoutineItems > 0 || updatedWorkoutItems > 0) {
            log.info(
                    "Normalized exerciseId values. routineItemsUpdated={}, workoutItemsUpdated={}",
                    updatedRoutineItems,
                    updatedWorkoutItems
            );
        }
    }
}
