package com.fitflow.backend.service;

import com.fitflow.backend.dto.WorkoutDayCountDto;
import com.fitflow.backend.dto.WorkoutDayCountsResponse;
import com.fitflow.backend.entity.WorkoutDay;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.WorkoutDayRepository;
import com.fitflow.backend.repository.WorkoutItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .isInstanceOf(CustomException.class)
                .hasMessage("from은 to보다 이후일 수 없습니다.");
    }
}
