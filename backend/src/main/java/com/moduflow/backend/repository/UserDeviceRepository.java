package com.moduflow.backend.repository;

import com.moduflow.backend.entity.UserDevice;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {

    Optional<UserDevice> findByAndroidId(String androidId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from UserDevice d where d.androidId = :androidId")
    Optional<UserDevice> findByAndroidIdForUpdate(@Param("androidId") String androidId);
}
