package com.moduflow.backend.service;

import com.moduflow.backend.dto.DeviceRegistrationRequest;
import com.moduflow.backend.dto.DeviceRegistrationResponse;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserDevice;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserDeviceRepository;
import com.moduflow.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceRegistrationServiceTest {

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeviceRegistrationService deviceRegistrationService;

    @Test
    void registerStoresNormalizedAndroidIdForLoggedInUser() {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest();
        ReflectionTestUtils.setField(request, "androidId", " A1B2C3D4E5F6G7H8 ");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(userDeviceRepository.findByAndroidId("a1b2c3d4e5f6g7h8")).thenReturn(Optional.empty());
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceRegistrationResponse response = deviceRegistrationService.register(7L, request);

        ArgumentCaptor<UserDevice> captor = ArgumentCaptor.forClass(UserDevice.class);
        verify(userDeviceRepository).save(captor.capture());
        assertThat(captor.getValue().getAndroidId()).isEqualTo("a1b2c3d4e5f6g7h8");
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.maskedAndroidId()).isEqualTo("a1b2********g7h8");
    }

    @Test
    void registerAllowsDeviceAlreadyRegisteredToAnotherUser() {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest();
        ReflectionTestUtils.setField(request, "androidId", "android-abc");

        UserDevice existingDevice = new UserDevice("android-abc", 9L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(userDeviceRepository.findByAndroidId("android-abc"))
                .thenReturn(Optional.of(existingDevice));
        when(userDeviceRepository.save(any(UserDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeviceRegistrationResponse response = deviceRegistrationService.register(7L, request);

        ArgumentCaptor<UserDevice> captor = ArgumentCaptor.forClass(UserDevice.class);
        verify(userDeviceRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existingDevice);
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.maskedAndroidId()).isEqualTo("andr***-abc");
    }

    @Test
    void registerRejectsBlankAndroidId() {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest();
        ReflectionTestUtils.setField(request, "androidId", "   ");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));

        assertThatThrownBy(() -> deviceRegistrationService.register(7L, request))
                .isInstanceOf(CustomException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userDeviceRepository, never()).save(any());
    }

    private User user(Long userId) {
        return User.builder()
                .id(userId)
                .email("member@example.com")
                .password("encoded-password")
                .name("Member")
                .active(true)
                .build();
    }
}
