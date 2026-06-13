package com.moduflow.backend.service;

import com.moduflow.backend.dto.CurrentLocationResponse;
import com.moduflow.backend.dto.LocationRequest;
import com.moduflow.backend.dto.LocationUpdateResponse;
import com.moduflow.backend.dto.AutoAttendanceResultResponse;
import com.moduflow.backend.dto.AutoAttendanceStatus;
import com.moduflow.backend.entity.Attendance;
import com.moduflow.backend.entity.BeaconZone;
import com.moduflow.backend.entity.LocationHistory;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserDevice;
import com.moduflow.backend.entity.UserLocation;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.AttendanceRepository;
import com.moduflow.backend.repository.LocationHistoryRepository;
import com.moduflow.backend.repository.UserDeviceRepository;
import com.moduflow.backend.repository.UserLocationRepository;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.repository.UserSettingsRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private static final int MAX_USER_ID_LENGTH = 64;
    private static final long TRANSIENT_EXIT_GRACE_SECONDS = 30;
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final UserLocationRepository userLocationRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final BeaconZoneService beaconZoneService;
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final AttendanceRepository attendanceRepository;

    public LocationService(UserLocationRepository userLocationRepository,
                           LocationHistoryRepository locationHistoryRepository,
                           BeaconZoneService beaconZoneService,
                           UserDeviceRepository userDeviceRepository,
                           UserRepository userRepository,
                           UserSettingsRepository userSettingsRepository,
                           AttendanceRepository attendanceRepository) {
        this.userLocationRepository = userLocationRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.beaconZoneService = beaconZoneService;
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public LocationUpdateResponse updateLocation(LocationRequest request) {
        if (request == null) {
            throw badRequest("Request body is required.");
        }

        String userId = normalizeUserId(request.getUserId());
        Integer zoneId = request.getZoneId();
        String beaconId = BeaconZones.beaconIdOf(zoneId);
        String zoneName = zoneNameOf(zoneId, beaconId);
        String gymName = normalizeGymName(request.getGymName());
        LocalDateTime receivedAt = LocalDateTime.now();

        UserLocation userLocation = userLocationRepository.findById(userId)
                .orElseGet(() -> new UserLocation(userId));

        boolean currentLocationUpdated = shouldUpdateCurrentLocation(userLocation, zoneId, receivedAt);
        if (currentLocationUpdated) {
            userLocation.changeLocation(zoneId, beaconId, zoneName, gymName);
            userLocationRepository.save(userLocation);
        }
        locationHistoryRepository.save(new LocationHistory(userId, gymName, beaconId, zoneId, zoneName));

        AutoAttendanceResultResponse attendance = processAutoAttendance(userId, gymName, zoneId, zoneName);
        return new LocationUpdateResponse(currentLocationUpdated, attendance);
    }

    @Transactional(readOnly = true)
    public CurrentLocationResponse getCurrentLocation(String userId) {
        String normalizedUserId = normalizeUserId(userId);

        UserLocation userLocation = userLocationRepository.findById(normalizedUserId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "LOCATION_NOT_FOUND",
                        "Current location was not found."
                ));

        return new CurrentLocationResponse(
                userLocation.getUserId(),
                userLocation.getGymName(),
                userLocation.getBeaconId(),
                userLocation.getZoneId(),
                userLocation.getZoneName(),
                userLocation.getUpdatedAt()
        );
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw badRequest("userId is required.");
        }

        String normalized = userId.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_USER_ID_LENGTH) {
            throw badRequest("userId must be 64 characters or less.");
        }

        return normalized;
    }

    private boolean shouldUpdateCurrentLocation(UserLocation userLocation, Integer nextZoneId, LocalDateTime receivedAt) {
        if (!BeaconZones.isExitZone(nextZoneId)) {
            return true;
        }
        if (userLocation.getZoneId() == null || BeaconZones.isExitZone(userLocation.getZoneId())) {
            return true;
        }

        LocalDateTime previousUpdatedAt = userLocation.getUpdatedAt();
        if (previousUpdatedAt == null) {
            return true;
        }

        return !previousUpdatedAt.isAfter(receivedAt.minusSeconds(TRANSIENT_EXIT_GRACE_SECONDS));
    }

    private AutoAttendanceResultResponse processAutoAttendance(String androidId, String gymName, Integer zoneId, String zoneName) {
        if (BeaconZones.isExitZone(zoneId)) {
            return attendanceResult(AutoAttendanceStatus.NO_ACTION, null);
        }

        UserDevice device = userDeviceRepository.findByAndroidIdForUpdate(androidId)
                .orElse(null);
        if (device == null) {
            return attendanceResult(AutoAttendanceStatus.DEVICE_NOT_REGISTERED, null);
        }

        User user = userRepository.findByIdForUpdate(device.getUserId())
                .orElse(null);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            return attendanceResult(AutoAttendanceStatus.DEVICE_NOT_REGISTERED, null);
        }

        boolean autoAttendanceEnabled = userSettingsRepository.findByUserId(user.getId())
                .map(settings -> settings.isAutoAttendanceEnabled())
                .orElse(false);
        if (!autoAttendanceEnabled) {
            return attendanceResult(AutoAttendanceStatus.AUTO_ATTENDANCE_DISABLED, null);
        }

        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDateTime startAt = today.atStartOfDay();
        LocalDateTime endAt = today.plusDays(1).atStartOfDay();

        Attendance existing = attendanceRepository
                .findFirstByUserIdAndGymNameAndCheckOutTimeIsNullAndCheckInTimeGreaterThanEqualAndCheckInTimeLessThanOrderByCheckInTimeAsc(
                        user.getId(),
                        gymName,
                        startAt,
                        endAt
                )
                .orElse(null);
        if (existing != null) {
            return attendanceResult(AutoAttendanceStatus.ALREADY_CHECKED_IN, existing);
        }

        Attendance attendance = new Attendance(user.getId(), displayName(user), gymName);
        attendance.setZoneName(zoneName);
        attendance.setCheckInTime(LocalDateTime.now(SEOUL_ZONE));

        Attendance saved = attendanceRepository.save(attendance);
        return attendanceResult(AutoAttendanceStatus.CREATED, saved);
    }

    private AutoAttendanceResultResponse attendanceResult(AutoAttendanceStatus status, Attendance attendance) {
        if (attendance == null) {
            return new AutoAttendanceResultResponse(status, null, null);
        }
        return new AutoAttendanceResultResponse(
                status,
                attendance.getId(),
                toSeoulOffset(attendance.getCheckInTime())
        );
    }

    private OffsetDateTime toSeoulOffset(LocalDateTime checkInTime) {
        if (checkInTime == null) {
            return null;
        }
        return checkInTime.atZone(SEOUL_ZONE).toOffsetDateTime();
    }

    private String displayName(User user) {
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        return user.getEmail();
    }

    private String zoneNameOf(Integer zoneId, String beaconId) {
        if (zoneId == null) {
            throw badRequest("zoneId is required.");
        }

        if (BeaconZones.isExitZone(zoneId)) {
            return BeaconZones.EXIT_ZONE_NAME;
        }

        return beaconZoneService.findByBeaconId(beaconId)
                .map(BeaconZone::getZoneName)
                .orElseThrow(() -> badRequest("Undefined zoneId."));
    }

    private String normalizeGymName(String gymName) {
        if (gymName == null || gymName.isBlank()) {
            return BeaconZones.DEFAULT_GYM_NAME;
        }
        return gymName.trim();
    }

    private CustomException badRequest(String message) {
        return new CustomException(HttpStatus.BAD_REQUEST, "LOCATION_BAD_REQUEST", message);
    }
}
