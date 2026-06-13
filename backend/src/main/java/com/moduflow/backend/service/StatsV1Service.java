package com.moduflow.backend.service;

import com.moduflow.backend.dto.MonthlySummaryResponse;
import com.moduflow.backend.dto.WorkoutDaysResponse;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.WorkoutDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsV1Service {

    private final WorkoutDayRepository workoutDayRepository;

    public MonthlySummaryResponse monthlySummary(Long userId, String month) {
        YearMonth ym = parseMonth(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        int workoutDays = (int) workoutDayRepository.findByUserIdAndWorkoutDateBetweenAndWorkoutCountGreaterThanOrderByWorkoutDateDesc(userId, from, to, 0).stream()
                .map(day -> day.getWorkoutDate())
                .distinct()
                .count();

        int totalDaysInMonth = ym.lengthOfMonth();
        int attendanceRate = Math.round((workoutDays * 100.0f) / totalDaysInMonth);
        return new MonthlySummaryResponse(ym.toString(), attendanceRate, workoutDays, totalDaysInMonth);
    }

    public WorkoutDaysResponse workoutDays(Long userId, String month) {
        YearMonth ym = parseMonth(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        List<String> days = workoutDayRepository.findByUserIdAndWorkoutDateBetweenAndWorkoutCountGreaterThanOrderByWorkoutDateDesc(userId, from, to, 0).stream()
                .map(day -> day.getWorkoutDate().toString())
                .distinct()
                .sorted()
                .toList();
        return new WorkoutDaysResponse(ym.toString(), days);
    }

    private YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "month는 YYYY-MM 형식이어야 합니다.");
        }
    }
}
