package com.moduflow.backend.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

final class AdminDateRanges {

    static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private AdminDateRanges() {
    }

    static AdminDateRange of(LocalDate date, Clock clock) {
        LocalDate resolvedDate = date == null ? LocalDate.now(clock.withZone(SEOUL)) : date;
        return new AdminDateRange(
                resolvedDate,
                resolvedDate.atStartOfDay(),
                resolvedDate.plusDays(1).atStartOfDay()
        );
    }

    record AdminDateRange(LocalDate date, LocalDateTime startAt, LocalDateTime endAt) {
    }
}
