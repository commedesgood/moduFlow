package com.moduflow.backend.service;

import com.moduflow.backend.dto.RoutineItemDto;
import com.moduflow.backend.dto.RoutineScheduleDto;
import com.moduflow.backend.entity.RoutineItem;
import com.moduflow.backend.repository.RoutineItemRepository;
import com.moduflow.backend.repository.RoutineRestDayRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutinesV1ServiceTest {

    @Mock
    private RoutineItemRepository routineItemRepository;

    @Mock
    private RoutineRestDayRepository routineRestDayRepository;

    @InjectMocks
    private RoutinesV1Service routinesV1Service;

    @Test
    void saveRoutinesNormalizesExerciseIdBeforePersisting() {
        RoutineScheduleDto schedule = new RoutineScheduleDto(
                List.of(new RoutineItemDto(null, "Bench Press", 4, 8, 50.0, "bench-press")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        routinesV1Service.saveRoutines(7L, schedule);

        ArgumentCaptor<List<RoutineItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(routineItemRepository).saveAllAndFlush(captor.capture());
        assertThat(captor.getValue()).singleElement()
                .extracting(RoutineItem::getExerciseId)
                .isEqualTo("benchpress");
    }
}
