package com.fitflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class WorkoutRecordResponse {
    private Long id;
    private Long userId;
    private Long workoutId;
    private int reps;
    private int sets;
    private int duration;
    private int accuracy;
    private LocalDate date;
}
