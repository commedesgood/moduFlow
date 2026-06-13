package com.moduflow.backend.service;

import com.moduflow.backend.dto.OkResponse;
import com.moduflow.backend.dto.RoutineItemDto;
import com.moduflow.backend.dto.RoutineScheduleDto;
import com.moduflow.backend.entity.RoutineItem;
import com.moduflow.backend.entity.RoutineRestDay;
import com.moduflow.backend.repository.RoutineItemRepository;
import com.moduflow.backend.repository.RoutineRestDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutinesV1Service {

    private static final List<String> DAYS = List.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    private static final Map<String, Short> DAY_TO_INDEX = buildDayToIndex();

    private final RoutineItemRepository routineItemRepository;
    private final RoutineRestDayRepository routineRestDayRepository;

    @Transactional(readOnly = true)
    public RoutineScheduleDto getRoutines(Long userId) {
        Map<String, ArrayList<RoutineItemDto>> schedule = emptyMutableSchedule();

        for (RoutineItem item : routineItemRepository.findByUserIdOrderByDayOfWeekAscSortOrderAsc(userId)) {
            if (item.getDayOfWeek() < 0 || item.getDayOfWeek() >= DAYS.size()) {
                continue;
            }
            schedule.get(DAYS.get(item.getDayOfWeek())).add(toDto(item));
        }

        List<String> restDays = routineRestDayRepository.findByUserIdOrderByDayOfWeekAsc(userId).stream()
                .map(RoutineRestDay::getDayOfWeek)
                .filter(dayOfWeek -> dayOfWeek >= 0 && dayOfWeek < DAYS.size())
                .map(dayOfWeek -> DAYS.get(dayOfWeek))
                .toList();

        Map<String, List<RoutineItemDto>> normalizedSchedule = immutableSchedule(schedule);
        return new RoutineScheduleDto(
                normalizedSchedule.get("mon"),
                normalizedSchedule.get("tue"),
                normalizedSchedule.get("wed"),
                normalizedSchedule.get("thu"),
                normalizedSchedule.get("fri"),
                normalizedSchedule.get("sat"),
                normalizedSchedule.get("sun"),
                restDays
        );
    }

    @Transactional
    public OkResponse saveRoutines(Long userId, RoutineScheduleDto schedule) {
        try {
            log.info("Routine save started. userId={}, hasRestDays={}",
                    userId,
                    schedule != null && schedule.getRestDays() != null);

            Map<String, List<RoutineItemDto>> normalizedSchedule = normalizeSchedule(schedule);
            List<String> normalizedRestDays = normalizeRestDays(schedule);
            log.info("Routine save normalized. userId={}, itemCount={}, restDayCount={}",
                    userId,
                    countItems(normalizedSchedule),
                    normalizedRestDays.size());

            routineItemRepository.deleteAllByUserIdInBulk(userId);
            routineRestDayRepository.deleteAllByUserIdInBulk(userId);
            log.info("Routine save deleted existing items. userId={}", userId);

            int savedCount = 0;
            List<RoutineItem> entities = new ArrayList<>();
            for (String day : DAYS) {
                short dayOfWeek = DAY_TO_INDEX.get(day);
                List<RoutineItemDto> items = normalizedSchedule.getOrDefault(day, List.of());
                for (int index = 0; index < items.size(); index++) {
                    entities.add(toEntity(userId, dayOfWeek, index, items.get(index)));
                    savedCount++;
                }
            }
            log.info("Routine save mapped entities. userId={}, itemCount={}", userId, savedCount);
            routineItemRepository.saveAllAndFlush(entities);
            routineRestDayRepository.saveAllAndFlush(toRestDayEntities(userId, normalizedRestDays));

            log.info("Saved routine items. userId={}, itemCount={}, restDayCount={}",
                    userId, savedCount, normalizedRestDays.size());
            return new OkResponse(true);
        } catch (Exception e) {
            log.error("Routine save failed. userId={}, hasRestDays={}",
                    userId,
                    schedule != null && schedule.getRestDays() != null,
                    e);
            throw e;
        }
    }

    private RoutineItem toEntity(Long userId, short dayOfWeek, int sortOrder, RoutineItemDto dto) {
        return RoutineItem.builder()
                .id(normalizeId(dto.getId()))
                .userId(userId)
                .dayOfWeek(dayOfWeek)
                .exerciseId(ExerciseIdNormalizer.normalize(dto.getExerciseId()))
                .name(dto.getName())
                .sets(dto.getSets())
                .reps(dto.getReps())
                .weight(toBigDecimal(dto.getWeight()))
                .sortOrder(sortOrder)
                .build();
    }

    private RoutineItemDto toDto(RoutineItem item) {
        return new RoutineItemDto(
                item.getId(),
                item.getName(),
                item.getSets(),
                item.getReps(),
                item.getWeight() == null ? null : item.getWeight().doubleValue(),
                ExerciseIdNormalizer.normalize(item.getExerciseId())
        );
    }

    private Map<String, List<RoutineItemDto>> normalizeSchedule(RoutineScheduleDto schedule) {
        Map<String, ArrayList<RoutineItemDto>> map = emptyMutableSchedule();
        if (schedule == null) {
            return immutableSchedule(map);
        }

        addItems(map, "mon", schedule.getMon());
        addItems(map, "tue", schedule.getTue());
        addItems(map, "wed", schedule.getWed());
        addItems(map, "thu", schedule.getThu());
        addItems(map, "fri", schedule.getFri());
        addItems(map, "sat", schedule.getSat());
        addItems(map, "sun", schedule.getSun());

        return immutableSchedule(map);
    }

    private List<String> normalizeRestDays(RoutineScheduleDto schedule) {
        if (schedule == null || schedule.getRestDays() == null) {
            return List.of();
        }

        LinkedHashSet<String> uniqueDays = new LinkedHashSet<>();
        for (String day : schedule.getRestDays()) {
            String canonicalDay = canonicalDay(day);
            if (canonicalDay != null) {
                uniqueDays.add(canonicalDay);
            }
        }
        return List.copyOf(uniqueDays);
    }

    private void addItems(Map<String, ArrayList<RoutineItemDto>> schedule, String day, List<RoutineItemDto> items) {
        if (items == null) {
            return;
        }

        for (RoutineItemDto item : items) {
            if (item == null || item.getName() == null || item.getName().isBlank()) {
                continue;
            }
            schedule.get(day).add(item);
        }
    }

    private List<RoutineRestDay> toRestDayEntities(Long userId, List<String> restDays) {
        List<RoutineRestDay> entities = new ArrayList<>();
        for (String day : restDays) {
            entities.add(RoutineRestDay.builder()
                    .userId(userId)
                    .dayOfWeek(DAY_TO_INDEX.get(day))
                    .build());
        }
        return entities;
    }

    private Map<String, ArrayList<RoutineItemDto>> emptyMutableSchedule() {
        Map<String, ArrayList<RoutineItemDto>> map = new LinkedHashMap<>();
        for (String day : DAYS) {
            map.put(day, new ArrayList<>());
        }
        return map;
    }

    private Map<String, List<RoutineItemDto>> immutableSchedule(Map<String, ArrayList<RoutineItemDto>> source) {
        Map<String, List<RoutineItemDto>> map = new LinkedHashMap<>();
        for (String day : DAYS) {
            map.put(day, List.copyOf(source.getOrDefault(day, new ArrayList<>())));
        }
        return map;
    }

    private static Map<String, Short> buildDayToIndex() {
        Map<String, Short> map = new LinkedHashMap<>();
        for (short index = 0; index < DAYS.size(); index++) {
            map.put(DAYS.get(index), index);
        }
        map.put("monday", (short) 0);
        map.put("tuesday", (short) 1);
        map.put("wednesday", (short) 2);
        map.put("thursday", (short) 3);
        map.put("friday", (short) 4);
        map.put("saturday", (short) 5);
        map.put("sunday", (short) 6);
        return java.util.Collections.unmodifiableMap(map);
    }

    private String canonicalDay(String key) {
        if (key == null) {
            return null;
        }
        Short index = DAY_TO_INDEX.get(key.trim().toLowerCase());
        return index == null ? null : DAYS.get(index);
    }

    private String normalizeId(String id) {
        return id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private int countItems(Map<String, List<RoutineItemDto>> schedule) {
        if (schedule == null || schedule.isEmpty()) {
            return 0;
        }
        return schedule.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .sum();
    }
}
