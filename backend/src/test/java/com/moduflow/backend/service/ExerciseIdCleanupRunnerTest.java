package com.moduflow.backend.service;

import com.moduflow.backend.repository.RoutineItemRepository;
import com.moduflow.backend.repository.WorkoutItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseIdCleanupRunnerTest {

    @Mock
    private RoutineItemRepository routineItemRepository;

    @Mock
    private WorkoutItemRepository workoutItemRepository;

    @InjectMocks
    private ExerciseIdCleanupRunner exerciseIdCleanupRunner;

    @Test
    void runNormalizesExistingExerciseIdsInBothTables() throws Exception {
        when(routineItemRepository.normalizeExerciseIds()).thenReturn(2);
        when(workoutItemRepository.normalizeExerciseIds()).thenReturn(3);

        exerciseIdCleanupRunner.run(new DefaultApplicationArguments(new String[0]));

        verify(routineItemRepository).normalizeExerciseIds();
        verify(workoutItemRepository).normalizeExerciseIds();
    }
}
