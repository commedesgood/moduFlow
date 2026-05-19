package com.fitflow.backend.repository;

import com.fitflow.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserIdOrderByCheckInTimeDesc(Long userId);

    List<Attendance> findByUserIdAndGymNameOrderByCheckInTimeDesc(Long userId, String gymName);

    List<Attendance> findByGymName(String gymName);

    List<Attendance> findByGymNameAndCheckInTimeAfter(
            String gymName,
            LocalDateTime time
    );

    List<Attendance> findByGymNameAndCheckOutTimeIsNull(String gymName);

    long countByGymNameAndCheckOutTimeIsNull(String gymName);

    long countByGymNameAndCheckInTimeAfter(String gymName, LocalDateTime time);

    Optional<Attendance> findByIdAndUserId(Long id, Long userId);

    Optional<Attendance> findByUserNameAndGymNameAndCheckOutTimeIsNull(
            String userName,
            String gymName
    );

    Optional<Attendance> findByUserIdAndGymNameAndCheckOutTimeIsNull(
            Long userId,
            String gymName
    );
}

