package com.moduflow.backend.repository;

import com.moduflow.backend.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface UserLocationRepository extends JpaRepository<UserLocation, String> {

    void deleteByBeaconId(String beaconId);

    @Query("""
            select u.beaconId as beaconId, count(u) as peopleCount
            from UserLocation u
            where u.gymName = :gymName
              and u.zoneId <> 0
              and u.beaconId is not null
              and u.updatedAt >= :activeSince
            group by u.beaconId
            """)
    List<BeaconUserCount> countCurrentUsersByGymNameGroupedByBeaconId(
            @Param("gymName") String gymName,
            @Param("activeSince") LocalDateTime activeSince
    );

    interface BeaconUserCount {
        String getBeaconId();

        long getPeopleCount();
    }
}
