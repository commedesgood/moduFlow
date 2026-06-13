package com.moduflow.backend.service;

import com.moduflow.backend.dto.WorkoutDayCountDto;
import com.moduflow.backend.dto.WorkoutDayCountsResponse;
import com.moduflow.backend.dto.WorkoutItemDto;
import com.moduflow.backend.dto.WorkoutItemsRequest;
import com.moduflow.backend.entity.WorkoutDay;
import com.moduflow.backend.entity.WorkoutItem;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.WorkoutDayRepository;
import com.moduflow.backend.repository.WorkoutItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutsV1ServiceTest {

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private WorkoutItemRepository workoutItemRepository;

    @InjectMocks
    private WorkoutsV1Service workoutsV1Service;

    @Test
    void incrementDayCountDeletesEmptyDayWhenCountBecomesZero() {
        LocalDate date = LocalDate.of(2026, 5, 14);
        WorkoutDay existing = WorkoutDay.builder()
                .id("day-1")
                .userId(7L)
                .workoutDate(date)
                .workoutCount(1)
                .build();

        when(workoutDayRepository.findByUserIdAndWorkoutDate(7L, date)).thenReturn(Optional.of(existing));
        when(workoutItemRepository.existsByWorkoutDayId("day-1")).thenReturn(false);

        WorkoutDayCountDto response = workoutsV1Service.incrementDayCount(7L, date, -1);

        assertThat(response.getDate()).isEqualTo("2026-05-14");
        assertThat(response.getWorkoutCount()).isZero();
        verify(workoutDayRepository).delete(existing);
        verify(workoutDayRepository, never()).save(existing);
    }

    @Test
    void getDayCountsBetweenFiltersZeroCountRows() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);
        WorkoutDay day = WorkoutDay.builder()
                .id("day-1")
                .userId(7L)
                .workoutDate(LocalDate.of(2026, 5, 14))
                .workoutCount(2)
                .build();

        when(workoutDayRepository.findByUserIdAndWorkoutDateBetweenAndWorkoutCountGreaterThanOrderByWorkoutDateDesc(7L, from, to, 0))
                .thenReturn(List.of(day));

        WorkoutDayCountsResponse response = workoutsV1Service.getDayCountsBetween(7L, from, to);

        assertThat(response.getCounts()).hasSize(1);
        assertThat(response.getCounts().get(0).getWorkoutCount()).isEqualTo(2);
    }

    @Test
    void getDayCountsBetweenRejectsInvalidDateRange() {
        assertThatThrownBy(() -> workoutsV1Service.getDayCountsBetween(
                7L,
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 1)
        ))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void putDayNormalizesExerciseIdBeforePersisting() {
        LocalDate date = LocalDate.of(2026, 5, 14);
        WorkoutItemsRequest request = new WorkoutItemsRequest();
        ReflectionTestUtils.setField(
                request,
                "items",
                List.of(new WorkoutItemDto(null, "bench-press", "Bench Press", null, 4, 8, 50.0))
        );

        WorkoutDay savedDay = WorkoutDay.builder()
                .id("day-1")
                .userId(7L)
                .workoutDate(date)
                .workoutCount(1)
                .build();

        when(workoutDayRepository.findByUserIdAndWorkoutDate(7L, date)).thenReturn(Optional.empty());
        when(workoutDayRepository.save(any(WorkoutDay.class))).thenReturn(savedDay);

        workoutsV1Service.putDay(7L, date, request);

        ArgumentCaptor<WorkoutItem> captor = ArgumentCaptor.forClass(WorkoutItem.class);
        verify(workoutItemRepository).save(captor.capture());
        assertThat(captor.getValue().getExerciseId()).isEqualTo("benchpress");
    }
}
