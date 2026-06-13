package com.moduflow.backend.service;

import com.moduflow.backend.entity.Attendance;
import com.moduflow.backend.repository.AttendanceRepository;
import com.moduflow.backend.repository.BeaconZoneRepository;
import com.moduflow.backend.repository.UserLocationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.moduflow.backend.dto.AttendanceRequest;
import com.moduflow.backend.dto.AttendanceResponse;
import com.moduflow.backend.dto.BeaconCongestionResponse;

import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserLocation;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private static final long ACTIVE_LOCATION_TTL_MINUTES = 2;

    private final AttendanceRepository attendanceRepository;
    private final UserLocationRepository userLocationRepository;
    private final BeaconZoneRepository beaconZoneRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             UserLocationRepository userLocationRepository,
                             BeaconZoneRepository beaconZoneRepository,
                             UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userLocationRepository = userLocationRepository;
        this.beaconZoneRepository = beaconZoneRepository;
        this.userRepository = userRepository;
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
        attendance.setZoneName(currentZoneNameOf(userId));

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
    public BeaconCongestionResponse getRecentCongestion(String gymName) {
        String normalizedGymName = normalizeGymName(gymName);
        LocalDateTime activeSince = LocalDateTime.now().minusMinutes(ACTIVE_LOCATION_TTL_MINUTES);
        Map<String, UserLocationRepository.BeaconUserCount> countsByBeaconId = userLocationRepository
                .countCurrentUsersByGymNameGroupedByBeaconId(normalizedGymName, activeSince)
                .stream()
                .collect(Collectors.toMap(UserLocationRepository.BeaconUserCount::getBeaconId, Function.identity()));

        List<BeaconCongestionResponse.BeaconCongestionItem> beacons = beaconZoneRepository.findAllByOrderByBeaconIdAsc().stream()
                .map(zone -> {
                    UserLocationRepository.BeaconUserCount count = countsByBeaconId.get(zone.getBeaconId());
                    long peopleCount = count == null ? 0 : count.getPeopleCount();
                    return new BeaconCongestionResponse.BeaconCongestionItem(
                            zone.getBeaconId(),
                            zone.getZoneId(),
                            zone.getZoneName(),
                            peopleCount,
                            zone.getCapacity(),
                            peopleCount,
                            congestionLevelOf(peopleCount, zone.getCapacity())
                    );
                })
                .toList();

        return new BeaconCongestionResponse(beacons, Instant.now());
    }

    private int congestionLevelOf(long peopleCount, int capacity) {
        if (peopleCount <= 0 || capacity <= 0) {
            return 0;
        }

        double ratio = (double) peopleCount / capacity;
        if (ratio <= 1.0 / 3.0) {
            return 0;
        }
        if (ratio <= 2.0 / 3.0) {
            return 1;
        }
        return 2;
    }

    private String displayName(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return email;
    }

    private String currentZoneNameOf(Long userId) {
        if (userId == null) {
            return null;
        }

        String locationUserId = userRepository.findById(userId)
                .map(this::locationUserIdOf)
                .orElse(String.valueOf(userId));

        return userLocationRepository.findById(locationUserId)
                .map(UserLocation::getZoneName)
                .orElse(null);
    }

    private String locationUserIdOf(User user) {
        if (user.getProviderId() != null && !user.getProviderId().isBlank()) {
            return user.getProviderId();
        }
        return String.valueOf(user.getId());
    }

    private String normalizeGymName(String gymName) {
        if (gymName == null || gymName.isBlank()) {
            throw new CustomException("gymName은 필수입니다.");
        }
        return gymName.trim();
    }
}
