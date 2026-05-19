package com.fitflow.backend.entity;

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
                @Index(name = "idx_attendance_user_gym_checkout", columnList = "userId, gymName, checkOutTime"),
                @Index(name = "idx_attendance_gym_checkout", columnList = "gymName, checkOutTime"),
                @Index(name = "idx_attendance_gym_checkin", columnList = "gymName, checkInTime")
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
