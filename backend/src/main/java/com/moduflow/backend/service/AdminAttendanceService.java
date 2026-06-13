package com.moduflow.backend.service;

import com.moduflow.backend.dto.AdminAttendanceItemResponse;
import com.moduflow.backend.dto.AdminAttendancePageResponse;
import com.moduflow.backend.dto.AdminAttendanceStatus;
import com.moduflow.backend.entity.Attendance;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.AttendanceRepository;
import com.moduflow.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AdminAttendanceService {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final Clock clock;

    public AdminAttendanceService(UserRepository userRepository,
                                  AttendanceRepository attendanceRepository,
                                  Clock clock) {
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminAttendancePageResponse getAttendances(LocalDate date,
                                                      AdminAttendanceStatus status,
                                                      String keyword,
                                                      int page,
                                                      int size) {
        validatePage(page, size);
        AdminDateRanges.AdminDateRange range = AdminDateRanges.of(date, clock);

        Page<UserRepository.AdminAttendanceUserProjection> users =
                userRepository.findAdminAttendanceUsers(
                        UserRole.USER,
                        range.startAt(),
                        range.endAt(),
                        status == null ? null : status.name(),
                        normalizeKeyword(keyword),
                        PageRequest.of(page, size)
                );

        Map<Long, Attendance> firstAttendanceByUserId = firstAttendanceByUserId(
                users.getContent().stream()
                        .map(UserRepository.AdminAttendanceUserProjection::getUserId)
                        .toList(),
                range.startAt(),
                range.endAt()
        );

        List<AdminAttendanceItemResponse> content = users.getContent().stream()
                .map(user -> toResponse(user, firstAttendanceByUserId.get(user.getUserId())))
                .toList();

        return new AdminAttendancePageResponse(
                content,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    private Map<Long, Attendance> firstAttendanceByUserId(List<Long> userIds,
                                                          LocalDateTime startAt,
                                                          LocalDateTime endAt) {
        Map<Long, Attendance> result = new LinkedHashMap<>();
        if (userIds.isEmpty()) {
            return result;
        }

        attendanceRepository.findFirstAttendancesForUsers(userIds, startAt, endAt)
                .forEach(attendance -> result.putIfAbsent(attendance.getUserId(), attendance));
        return result;
    }

    private AdminAttendanceItemResponse toResponse(UserRepository.AdminAttendanceUserProjection user,
                                                   Attendance firstAttendance) {
        AdminAttendanceStatus status = firstAttendance == null
                ? AdminAttendanceStatus.ABSENT
                : AdminAttendanceStatus.ATTENDED;

        return new AdminAttendanceItemResponse(
                user.getUserId(),
                user.getEmail() == null ? "" : user.getEmail(),
                user.getName() == null ? "" : user.getName(),
                status,
                firstAttendance == null ? null : toSeoulOffset(firstAttendance.getCheckInTime()),
                firstAttendance == null ? null : firstAttendance.getZoneName()
        );
    }

    private OffsetDateTime toSeoulOffset(LocalDateTime checkInTime) {
        return checkInTime.atZone(AdminDateRanges.SEOUL).toOffsetDateTime();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "ADMIN_BAD_REQUEST", "page must be 0 or greater.");
        }
        if (size < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "ADMIN_BAD_REQUEST", "size must be 1 or greater.");
        }
    }

}
