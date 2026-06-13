package com.moduflow.backend.service;

import com.moduflow.backend.dto.AdminDashboardSummaryResponse;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final Clock clock;

    public AdminDashboardService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary(LocalDate date) {
        AdminDateRanges.AdminDateRange range = AdminDateRanges.of(date, clock);

        long totalMembers = userRepository.countActiveRegularMembers(UserRole.USER);
        long checkedInCount = userRepository.countActiveRegularMembersAttendedBetween(
                UserRole.USER,
                range.startAt(),
                range.endAt()
        );
        long absentCount = totalMembers - checkedInCount;

        return new AdminDashboardSummaryResponse(
                totalMembers,
                checkedInCount,
                absentCount,
                attendanceRate(totalMembers, checkedInCount)
        );
    }

    private double attendanceRate(long totalMembers, long checkedInCount) {
        if (totalMembers == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(checkedInCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalMembers), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
