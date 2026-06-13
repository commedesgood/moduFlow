package com.moduflow.backend.repository;

import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    @Query(value = """
            select u.id as userId, u.email as email, u.name as name
            from User u
            where (u.role is null or u.role = :regularRole)
              and (u.active is null or u.active = true)
              and (
                    :keyword is null
                    or lower(coalesce(u.name, '')) like concat('%', :keyword, '%')
                    or lower(u.email) like concat('%', :keyword, '%')
                  )
              and (
                    :status is null
                    or (:status = 'ATTENDED' and exists (
                        select a.id
                        from Attendance a
                        where a.userId = u.id
                          and a.checkInTime >= :startAt
                          and a.checkInTime < :endAt
                    ))
                    or (:status = 'ABSENT' and not exists (
                        select a.id
                        from Attendance a
                        where a.userId = u.id
                          and a.checkInTime >= :startAt
                          and a.checkInTime < :endAt
                    ))
                  )
            order by u.id asc
            """,
            countQuery = """
            select count(u.id)
            from User u
            where (u.role is null or u.role = :regularRole)
              and (u.active is null or u.active = true)
              and (
                    :keyword is null
                    or lower(coalesce(u.name, '')) like concat('%', :keyword, '%')
                    or lower(u.email) like concat('%', :keyword, '%')
                  )
              and (
                    :status is null
                    or (:status = 'ATTENDED' and exists (
                        select a.id
                        from Attendance a
                        where a.userId = u.id
                          and a.checkInTime >= :startAt
                          and a.checkInTime < :endAt
                    ))
                    or (:status = 'ABSENT' and not exists (
                        select a.id
                        from Attendance a
                        where a.userId = u.id
                          and a.checkInTime >= :startAt
                          and a.checkInTime < :endAt
                    ))
                  )
            """)
    Page<AdminAttendanceUserProjection> findAdminAttendanceUsers(
            @Param("regularRole") UserRole regularRole,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            select count(u.id)
            from User u
            where (u.role is null or u.role = :regularRole)
              and (u.active is null or u.active = true)
            """)
    long countActiveRegularMembers(@Param("regularRole") UserRole regularRole);

    @Query("""
            select count(u.id)
            from User u
            where (u.role is null or u.role = :regularRole)
              and (u.active is null or u.active = true)
              and exists (
                  select a.id
                  from Attendance a
                  where a.userId = u.id
                    and a.checkInTime >= :startAt
                    and a.checkInTime < :endAt
              )
            """)
    long countActiveRegularMembersAttendedBetween(
            @Param("regularRole") UserRole regularRole,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    interface AdminAttendanceUserProjection {
        Long getUserId();

        String getEmail();

        String getName();
    }
}
