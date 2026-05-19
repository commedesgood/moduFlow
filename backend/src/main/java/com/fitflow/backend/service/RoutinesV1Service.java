package com.fitflow.backend.service;

import com.fitflow.backend.dto.OkResponse;
import com.fitflow.backend.dto.RoutineItemDto;
import com.fitflow.backend.entity.RoutineItem;
import com.fitflow.backend.repository.RoutineItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.ArrayList;
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

    @Transactional(readOnly = true)
    public Map<String, List<RoutineItemDto>> getRoutines(Long userId) {
        Map<String, java.util.ArrayList<RoutineItemDto>> schedule = emptyMutableSchedule();

        for (RoutineItem item : routineItemRepository.findByUserIdOrderByDayOfWeekAscSortOrderAsc(userId)) {
            if (item.getDayOfWeek() < 0 || item.getDayOfWeek() >= DAYS.size()) {
                continue;
            }
            schedule.get(DAYS.get(item.getDayOfWeek())).add(toDto(item));
        }

        return immutableSchedule(schedule);
    }

    @Transactional
    public OkResponse saveRoutines(Long userId, Map<String, List<RoutineItemDto>> schedule) {
        try {
            log.info("Routine save started. userId={}, requestDays={}", userId, schedule == null ? 0 : schedule.keySet());
            Map<String, List<RoutineItemDto>> normalized = normalize(schedule);
            log.info("Routine save normalized. userId={}, itemCount={}", userId, countItems(normalized));

            routineItemRepository.deleteAllByUserIdInBulk(userId);
            log.info("Routine save deleted existing items. userId={}", userId);

            int savedCount = 0;
            List<RoutineItem> entities = new ArrayList<>();
            for (String day : DAYS) {
                short dayOfWeek = DAY_TO_INDEX.get(day);
                List<RoutineItemDto> items = normalized.getOrDefault(day, List.of());
                for (int index = 0; index < items.size(); index++) {
                    entities.add(toEntity(userId, dayOfWeek, index, items.get(index)));
                    savedCount++;
                }
            }
            log.info("Routine save mapped entities. userId={}, itemCount={}", userId, savedCount);
            routineItemRepository.saveAllAndFlush(entities);

            log.info("Saved routine items. userId={}, itemCount={}", userId, savedCount);
            return new OkResponse(true);
        } catch (Exception e) {
            log.error("Routine save failed. userId={}, requestDays={}", userId, schedule == null ? 0 : schedule.keySet(), e);
            throw e;
        }
    }

    private RoutineItem toEntity(Long userId, short dayOfWeek, int sortOrder, RoutineItemDto dto) {
        return RoutineItem.builder()
                .id(normalizeId(dto.getId()))
                .userId(userId)
                .dayOfWeek(dayOfWeek)
                .exerciseId(blankToNull(dto.getExerciseId()))
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
                item.getExerciseId()
        );
    }

    private Map<String, List<RoutineItemDto>> normalize(Map<String, List<RoutineItemDto>> schedule) {
        Map<String, java.util.ArrayList<RoutineItemDto>> map = emptyMutableSchedule();
        if (schedule == null) {
            return immutableSchedule(map);
        }

        for (Map.Entry<String, List<RoutineItemDto>> entry : schedule.entrySet()) {
            String day = canonicalDay(entry.getKey());
            if (day == null || entry.getValue() == null) {
                continue;
            }
            for (RoutineItemDto item : entry.getValue()) {
                if (item == null || item.getName() == null || item.getName().isBlank()) {
                    continue;
                }
                map.get(day).add(item);
            }
        }

        return immutableSchedule(map);
    }

    private Map<String, java.util.ArrayList<RoutineItemDto>> emptyMutableSchedule() {
        Map<String, java.util.ArrayList<RoutineItemDto>> map = new HashMap<>();
        for (String day : DAYS) {
            map.put(day, new java.util.ArrayList<>());
        }
        return map;
    }

    private Map<String, List<RoutineItemDto>> immutableSchedule(Map<String, java.util.ArrayList<RoutineItemDto>> source) {
        Map<String, List<RoutineItemDto>> map = new HashMap<>();
        for (String day : DAYS) {
            map.put(day, List.copyOf(source.getOrDefault(day, new java.util.ArrayList<>())));
        }
        return map;
    }

    private static Map<String, Short> buildDayToIndex() {
        Map<String, Short> map = new HashMap<>();
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
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
