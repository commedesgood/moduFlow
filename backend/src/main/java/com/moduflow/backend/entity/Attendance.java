package com.moduflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "attendance",
        indexes = {
                @Index(name = "idx_attendance_user_checkin", columnList = "userId, checkInTime"),
                @Index(name = "idx_attendance_checkin_user", columnList = "checkInTime, userId"),
                @Index(name = "idx_attendance_user_gym_checkout", columnList = "userId, gymName, checkOutTime"),
                @Index(name = "idx_attendance_gym_checkout", columnList = "gymName, checkOutTime"),
                @Index(name = "idx_attendance_gym_checkin", columnList = "gymName, checkInTime"),
                @Index(name = "idx_attendance_auto_open", columnList = "userId, gymName, checkOutTime, checkInTime")
        }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    private String userName;
    private String gymName;

    @Column(length = 100)
    private String zoneName;

    private LocalDateTime checkInTime = LocalDateTime.now();
    private LocalDateTime checkOutTime;

    public Attendance(String userName, String gymName) {
        this(null, userName, gymName);
    }

    public Attendance(Long userId, String userName, String gymName) {
        this.userId = userId;
        this.userName = userName;
        this.gymName = gymName;
        this.checkInTime = LocalDateTime.now();
        this.checkOutTime = null;
    }
}
