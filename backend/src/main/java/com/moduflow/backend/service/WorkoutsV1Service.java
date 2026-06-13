package com.moduflow.backend.service;

import com.moduflow.backend.dto.OkResponse;
import com.moduflow.backend.dto.WorkoutDayCountDto;
import com.moduflow.backend.dto.WorkoutDayCountsResponse;
import com.moduflow.backend.dto.WorkoutDayDto;
import com.moduflow.backend.dto.WorkoutDaysResponse;
import com.moduflow.backend.dto.WorkoutItemDto;
import com.moduflow.backend.dto.WorkoutItemPatchRequest;
import com.moduflow.backend.dto.WorkoutItemPatchResponse;
import com.moduflow.backend.dto.WorkoutItemsRequest;
import com.moduflow.backend.dto.WorkoutsResponse;
import com.moduflow.backend.entity.WorkoutDay;
import com.moduflow.backend.entity.WorkoutItem;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.WorkoutDayRepository;
import com.moduflow.backend.repository.WorkoutItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutsV1Service {

    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutItemRepository workoutItemRepository;

    @Transactional(readOnly = true)
    public WorkoutsResponse getBetween(Long userId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        List<WorkoutDay> days = workoutDayRepository.findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(userId, from, to);
        Map<String, List<WorkoutItem>> itemsByDayId = days.isEmpty()
                ? Map.of()
                : workoutItemRepository.findByWorkoutDayIdInOrderByWorkoutDayIdAscSortOrderAsc(
                                days.stream().map(WorkoutDay::getId).toList()
                        ).stream()
                        .collect(Collectors.groupingBy(WorkoutItem::getWorkoutDayId));

        List<WorkoutDayDto> workouts = days.stream()
                .map(day -> new WorkoutDayDto(
                        day.getWorkoutDate().toString(),
                        toDtos(itemsByDayId.getOrDefault(day.getId(), List.of())),
                        day.getWorkoutCount()
                ))
                .toList();

        return new WorkoutsResponse(workouts);
    }

    @Transactional(readOnly = true)
    public WorkoutDayDto getOne(Long userId, LocalDate date) {
        WorkoutDay day = workoutDayRepository.findByUserIdAndWorkoutDate(userId, date)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", "No workout record exists for this date."));

        return new WorkoutDayDto(
                day.getWorkoutDate().toString(),
                toDtos(workoutItemRepository.findByWorkoutDayIdOrderBySortOrderAsc(day.getId())),
                day.getWorkoutCount()
        );
    }

    @Transactional
    public OkResponse putDay(Long userId, LocalDate date, WorkoutItemsRequest request) {
        List<WorkoutItemDto> items = normalizeItems(request == null ? null : request.getItems());
        WorkoutDay existing = workoutDayRepository.findByUserIdAndWorkoutDate(userId, date).orElse(null);

        if (items.isEmpty()) {
            if (existing != null) {
                workoutItemRepository.deleteByWorkoutDayId(existing.getId());
                if (existing.getWorkoutCount() <= 0) {
                    workoutDayRepository.delete(existing);
                }
            }
            return new OkResponse(true);
        }

        WorkoutDay day = workoutDayRepository.save(WorkoutDay.builder()
                .id(existing == null ? null : existing.getId())
                .userId(userId)
                .workoutDate(date)
                .workoutCount(existing == null || existing.getWorkoutCount() <= 0 ? items.size() : existing.getWorkoutCount())
                .createdAt(existing == null ? null : existing.getCreatedAt())
                .updatedAt(existing == null ? null : existing.getUpdatedAt())
                .build());

        workoutItemRepository.deleteByWorkoutDayId(day.getId());
        for (int index = 0; index < items.size(); index++) {
            WorkoutItemDto item = items.get(index);
            if (item == null || item.getName() == null || item.getName().isBlank()) {
                continue;
            }
            workoutItemRepository.save(toEntity(day.getId(), index, item));
        }

        return new OkResponse(true);
    }

    @Transactional
    public WorkoutItemPatchResponse patchItem(Long userId, LocalDate date, String itemId, WorkoutItemPatchRequest request) {
        WorkoutDay day = workoutDayRepository.findByUserIdAndWorkoutDate(userId, date)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", "No workout record exists for this date."));

        WorkoutItem original = workoutItemRepository.findByWorkoutDayIdAndId(day.getId(), itemId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workout item was not found."));

        WorkoutItem updated = workoutItemRepository.save(WorkoutItem.builder()
                .id(original.getId())
                .workoutDayId(original.getWorkoutDayId())
                .exerciseId(ExerciseIdNormalizer.normalize(original.getExerciseId()))
                .name(original.getName())
                .note(original.getNote())
                .sets(request.getSets() == null ? original.getSets() : request.getSets())
                .reps(request.getReps() == null ? original.getReps() : request.getReps())
                .weight(request.getWeight() == null ? original.getWeight() : toBigDecimal(request.getWeight()))
                .durationSeconds(original.getDurationSeconds())
                .accuracy(original.getAccuracy())
                .sortOrder(original.getSortOrder())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build());

        return new WorkoutItemPatchResponse(date.toString(), toDto(updated));
    }

    @Transactional
    public OkResponse deleteItem(Long userId, LocalDate date, String itemId) {
        WorkoutDay day = workoutDayRepository.findByUserIdAndWorkoutDate(userId, date)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", "No workout record exists for this date."));

        WorkoutItem item = workoutItemRepository.findByWorkoutDayIdAndId(day.getId(), itemId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workout item was not found."));

        workoutItemRepository.delete(item);
        if (workoutItemRepository.findByWorkoutDayIdOrderBySortOrderAsc(day.getId()).isEmpty() && day.getWorkoutCount() <= 0) {
            workoutDayRepository.delete(day);
        }

        return new OkResponse(true);
    }

    @Transactional(readOnly = true)
    public WorkoutDaysResponse getWorkoutDays(Long userId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<String> days = workoutDayRepository.findByUserIdAndWorkoutDateBetweenAndWorkoutCountGreaterThanOrderByWorkoutDateDesc(userId, from, to, 0).stream()
                .map(day -> day.getWorkoutDate().toString())
                .distinct()
                .sorted()
                .toList();
        return new WorkoutDaysResponse(month.toString(), days);
    }

    @Transactional(readOnly = true)
    public WorkoutDayCountsResponse getDayCountsBetween(Long userId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        List<WorkoutDayCountDto> counts = workoutDayRepository.findByUserIdAndWorkoutDateBetweenAndWorkoutCountGreaterThanOrderByWorkoutDateDesc(userId, from, to, 0).stream()
                .map(day -> new WorkoutDayCountDto(day.getWorkoutDate().toString(), day.getWorkoutCount()))
                .toList();
        return new WorkoutDayCountsResponse(counts);
    }

    @Transactional
    public WorkoutDayCountDto incrementDayCount(Long userId, LocalDate date, int delta) {
        WorkoutDay existing = workoutDayRepository.findByUserIdAndWorkoutDate(userId, date).orElse(null);
        int current = existing == null ? 0 : existing.getWorkoutCount();
        int next = Math.max(0, current + delta);

        if (next == 0 && !hasWorkoutItems(existing)) {
            if (existing != null) {
                workoutDayRepository.delete(existing);
            }
            return new WorkoutDayCountDto(date.toString(), 0);
        }

        WorkoutDay saved = workoutDayRepository.save(WorkoutDay.builder()
                .id(existing == null ? null : existing.getId())
                .userId(userId)
                .workoutDate(date)
                .workoutCount(next)
                .createdAt(existing == null ? null : existing.getCreatedAt())
                .updatedAt(existing == null ? null : existing.getUpdatedAt())
                .build());

        return new WorkoutDayCountDto(saved.getWorkoutDate().toString(), saved.getWorkoutCount());
    }

    private WorkoutItem toEntity(String workoutDayId, int sortOrder, WorkoutItemDto dto) {
        return WorkoutItem.builder()
                .id(normalizeId(dto.getId()))
                .workoutDayId(workoutDayId)
                .exerciseId(ExerciseIdNormalizer.normalize(dto.getExerciseId()))
                .name(dto.getName())
                .note(blankToNull(dto.getNote()))
                .sets(dto.getSets())
                .reps(dto.getReps())
                .weight(toBigDecimal(dto.getWeight()))
                .sortOrder(sortOrder)
                .build();
    }

    private List<WorkoutItemDto> normalizeItems(List<WorkoutItemDto> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(item -> item != null && item.getName() != null && !item.getName().isBlank())
                .toList();
    }

    private boolean hasWorkoutItems(WorkoutDay day) {
        return day != null && workoutItemRepository.existsByWorkoutDayId(day.getId());
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "from은 to보다 이후일 수 없습니다.");
        }
    }

    private List<WorkoutItemDto> toDtos(List<WorkoutItem> items) {
        return items.stream().map(this::toDto).toList();
    }

    private WorkoutItemDto toDto(WorkoutItem item) {
        return new WorkoutItemDto(
                item.getId(),
                ExerciseIdNormalizer.normalize(item.getExerciseId()),
                item.getName(),
                item.getNote(),
                item.getSets(),
                item.getReps(),
                item.getWeight() == null ? null : item.getWeight().doubleValue()
        );
    }

    private String normalizeId(String id) {
        return id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
