package com.moduflow.backend.service;

import com.moduflow.backend.dto.ProfileNameUpdateRequest;
import com.moduflow.backend.dto.MeProfileResponse;
import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeService meService;

    @Test
    void updateNameTrimsAndSavesLoggedInUsersName() {
        User user = user(1L, "user@example.com", "Old Name");
        ProfileNameUpdateRequest request = request(" 새 이름 ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MeProfileResponse response = meService.updateName(1L, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(userCaptor.getValue().getName()).isEqualTo("새 이름");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.name()).isEqualTo("새 이름");
    }

    @Test
    void updateNameRejectsBlankAfterTrimWithBadRequest() {
        assertThatThrownBy(() -> meService.updateName(1L, request("   ")))
                .isInstanceOf(CustomException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getMeReturnsCurrentDatabaseName() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "user@example.com", "Saved Name")));

        MeProfileResponse response = meService.getMe(1L);

        assertThat(response.name()).isEqualTo("Saved Name");
    }

    private User user(Long id, String email, String name) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .name(name)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    private ProfileNameUpdateRequest request(String name) {
        ProfileNameUpdateRequest request = new ProfileNameUpdateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        return request;
    }
}
