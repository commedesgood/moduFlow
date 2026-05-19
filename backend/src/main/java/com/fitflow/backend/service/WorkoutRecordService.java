package com.fitflow.backend.service;

import com.fitflow.backend.dto.WorkoutRecordRequest;
import com.fitflow.backend.dto.WorkoutRecordResponse;
import com.fitflow.backend.entity.WorkoutRecord;
import com.fitflow.backend.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutRecordService {

    private final WorkoutRecordRepository workoutRecordRepository;

    public WorkoutRecordResponse save(Long userId, WorkoutRecordRequest request) {
        LocalDate recordDate = request.getDate() == null ? LocalDate.now() : request.getDate();

        WorkoutRecord saved = workoutRecordRepository.save(WorkoutRecord.builder()
                .userId(userId)
                .workoutId(request.getWorkoutId())
                .reps(request.getReps())
                .sets(request.getSets())
                .duration(request.getDuration())
                .accuracy(request.getAccuracy())
                .date(recordDate)
                .build());

        return toResponse(saved);
    }

    public List<WorkoutRecordResponse> getMyRecords(Long userId) {
        return workoutRecordRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private WorkoutRecordResponse toResponse(WorkoutRecord record) {
        return new WorkoutRecordResponse(
                record.getId(),
                record.getUserId(),
                record.getWorkoutId(),
                record.getReps(),
                record.getSets(),
                record.getDuration(),
                record.getAccuracy(),
                record.getDate()
        );
    }
}
