package com.fitflow.backend.service;

import com.fitflow.backend.dto.AuthLoginRequest;
import com.fitflow.backend.dto.AuthResponse;
import com.fitflow.backend.entity.AuthProvider;
import com.fitflow.backend.entity.User;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.UserRepository;
import com.fitflow.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthV1ServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthV1Service authV1Service;

    @Test
    void loginNormalizesEmailBeforeLookup() {
        AuthLoginRequest request = new AuthLoginRequest();
        ReflectionTestUtils.setField(request, "email", " USER@Example.COM ");
        ReflectionTestUtils.setField(request, "password", "password123");

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .name("User")
                .provider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com")).thenReturn("access-token");

        AuthResponse response = authV1Service.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void loginRejectsBlankPasswordHashWithoutCallingPasswordEncoder() {
        AuthLoginRequest request = new AuthLoginRequest();
        ReflectionTestUtils.setField(request, "email", "social@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");

        User user = User.builder()
                .id(1L)
                .email("social@example.com")
                .password("")
                .name("User")
                .provider(AuthProvider.GOOGLE)
                .providerId("google-id")
                .build();

        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authV1Service.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessage("Invalid email or password.");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
