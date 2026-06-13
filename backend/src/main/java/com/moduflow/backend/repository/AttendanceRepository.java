package com.moduflow.backend.repository;

import com.moduflow.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Optional<Attendance> findFirstByUserIdAndGymNameAndCheckOutTimeIsNullAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThanOrderByCheckInTimeAsc(
            Long userId,
            String gymName,
            LocalDateTime startAt,
            LocalDateTime endAt
    );

    @Query("""
            select a
            from Attendance a
            where a.userId in :userIds
              and a.checkInTime >= :startAt
              and a.checkInTime < :endAt
              and a.checkInTime = (
                  select min(a2.checkInTime)
                  from Attendance a2
                  where a2.userId = a.userId
                    and a2.checkInTime >= :startAt
                    and a2.checkInTime < :endAt
              )
            order by a.userId asc, a.id asc
            """)
    List<Attendance> findFirstAttendancesForUsers(
            @Param("userIds") List<Long> userIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
