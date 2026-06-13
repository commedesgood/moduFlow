package com.moduflow.backend.service;

import com.moduflow.backend.dto.AuthLoginRequest;
import com.moduflow.backend.dto.AuthResponse;
import com.moduflow.backend.dto.AuthSignupRequest;
import com.moduflow.backend.dto.GoogleIdTokenLoginRequest;
import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.security.GoogleIdTokenClaims;
import com.moduflow.backend.security.GoogleIdTokenVerificationException;
import com.moduflow.backend.security.GoogleIdTokenVerifier;
import com.moduflow.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthV1ServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private DeviceRegistrationService deviceRegistrationService;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @InjectMocks
    private AuthV1Service authV1Service;

    @Test
    void signupSavesProvidedDisplayName() {
        AuthSignupRequest request = new AuthSignupRequest();
        ReflectionTestUtils.setField(request, "email", " NEW@Example.COM ");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", " 홍길동 ");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(3L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .provider(user.getProvider())
                    .build();
        });
        when(jwtUtil.generateToken("new@example.com", UserRole.USER)).thenReturn("access-token");

        AuthResponse response = authV1Service.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("홍길동");
        assertThat(response.getUser().getName()).isEqualTo("홍길동");
    }

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
        when(jwtUtil.generateToken("user@example.com", UserRole.USER)).thenReturn("access-token");

        AuthResponse response = authV1Service.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("user@example.com");
        assertThat(response.getUser().getRole()).isEqualTo("USER");
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

    @Test
    void loginRegistersDeviceWhenEmailPasswordAndUserIdAreProvided() {
        AuthLoginRequest request = new AuthLoginRequest();
        ReflectionTestUtils.setField(request, "email", "member@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "userId", "A1B2C3D4");

        User user = User.builder()
                .id(7L)
                .email("member@example.com")
                .password("encoded-password")
                .name("Member")
                .provider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("member@example.com", UserRole.USER)).thenReturn("access-token");

        AuthResponse response = authV1Service.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getId()).isEqualTo("u_7");
        verify(deviceRegistrationService).register(eq(7L), eq("A1B2C3D4"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void googleIdTokenLoginCreatesGoogleUserAndRegistersDevice() {
        GoogleIdTokenLoginRequest request = new GoogleIdTokenLoginRequest();
        ReflectionTestUtils.setField(request, "idToken", "google-id-token");
        ReflectionTestUtils.setField(request, "userId", "A1B2C3D4");

        when(googleIdTokenVerifier.verify("google-id-token"))
                .thenReturn(new GoogleIdTokenClaims("google-sub", "new@example.com", "New User"));
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(9L)
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .provider(user.getProvider())
                    .providerId(user.getProviderId())
                    .role(user.getRole())
                    .build();
        });
        when(jwtUtil.generateToken("new@example.com", UserRole.USER)).thenReturn("access-token");

        AuthResponse response = authV1Service.loginWithGoogleIdToken(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(captor.getValue().getProviderId()).isEqualTo("google-sub");
        assertThat(captor.getValue().getPassword()).isEmpty();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        verify(deviceRegistrationService).register(9L, "A1B2C3D4");
    }

    @Test
    void googleIdTokenLoginRejectsInvalidTokenBeforeUserLookup() {
        GoogleIdTokenLoginRequest request = new GoogleIdTokenLoginRequest();
        ReflectionTestUtils.setField(request, "idToken", "bad-token");

        when(googleIdTokenVerifier.verify("bad-token"))
                .thenThrow(new GoogleIdTokenVerificationException("invalid"));

        assertThatThrownBy(() -> authV1Service.loginWithGoogleIdToken(request))
                .isInstanceOf(CustomException.class)
                .hasMessage("Invalid Google ID token.");

        verifyNoInteractions(userRepository);
    }
}
