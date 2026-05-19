package com.fitflow.backend.service;

import com.fitflow.backend.entity.Attendance;
import com.fitflow.backend.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.fitflow.backend.dto.AttendanceRequest;
import com.fitflow.backend.dto.AttendanceResponse;

import com.fitflow.backend.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    // 출석 저장
    @Transactional
    public AttendanceResponse save(Long userId, String name, String email, AttendanceRequest request) {
        String gymName = normalizeGymName(request.getGymName());

        boolean exists = attendanceRepository
                .findByUserIdAndGymNameAndCheckOutTimeIsNull(userId, gymName)
                .isPresent();

        if (exists) {
            throw new CustomException("이미 입장한 사용자입니다.");
        }

        Attendance attendance = new Attendance(
                userId,
                displayName(name, email),
                gymName
        );

        Attendance saved = attendanceRepository.save(attendance);

        return new AttendanceResponse(
                saved.getId(),
                saved.getUserName(),
                saved.getGymName(),
                saved.getCheckInTime(),
                saved.getCheckOutTime()
        );
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAll(Long userId, String gymName) {

        List<Attendance> list;

        if (gymName != null && !gymName.isBlank()) {
            list = attendanceRepository.findByUserIdAndGymNameOrderByCheckInTimeDesc(userId, gymName.trim());
        } else {
            list = attendanceRepository.findByUserIdOrderByCheckInTimeDesc(userId);
        }

        return list.stream()
                .map(a -> new AttendanceResponse(
                        a.getId(),
                        a.getUserName(),
                        a.getGymName(),
                        a.getCheckInTime(),
                        a.getCheckOutTime()
                ))
                .toList();
    }

    // 퇴장 기능
    @Transactional
    public void checkOut(Long userId, Long id) {

        Attendance attendance = attendanceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException("출석 기록이 없습니다."));

        if (attendance.getCheckOutTime() != null) {
            throw new CustomException("이미 퇴장한 출석 기록입니다.");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        attendanceRepository.save(attendance);
    }

    // 전체 혼잡도
    @Transactional(readOnly = true)
    public int getCongestion(String gymName) {
        return Math.toIntExact(attendanceRepository.countByGymNameAndCheckOutTimeIsNull(normalizeGymName(gymName)));
    }

    // 최근 1시간 혼잡도
    @Transactional(readOnly = true)
    public int getRecentCongestion(String gymName) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        return Math.toIntExact(attendanceRepository.countByGymNameAndCheckInTimeAfter(normalizeGymName(gymName), oneHourAgo));
    }

    private String displayName(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return email;
    }

    private String normalizeGymName(String gymName) {
        if (gymName == null || gymName.isBlank()) {
            throw new CustomException("gymName은 필수입니다.");
        }
        return gymName.trim();
    }
}
