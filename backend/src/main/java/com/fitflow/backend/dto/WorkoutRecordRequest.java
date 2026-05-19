package com.fitflow.backend.dto;

import lombok.Getter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Getter
public class WorkoutRecordRequest {
    @NotNull(message = "workoutId is required")
    @Positive(message = "workoutId must be positive")
    private Long workoutId;
    @Min(value = 0, message = "reps must be >= 0")
    private int reps;
    @Min(value = 0, message = "sets must be >= 0")
    private int sets;
    @Min(value = 0, message = "duration must be >= 0")
    private int duration;
    @Min(value = 0, message = "accuracy must be >= 0")
    @Max(value = 100, message = "accuracy must be <= 100")
    private int accuracy;
    private LocalDate date;
}
