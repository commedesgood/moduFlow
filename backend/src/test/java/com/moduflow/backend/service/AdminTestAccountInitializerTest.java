package com.moduflow.backend.service;

import com.moduflow.backend.entity.AuthProvider;
import com.moduflow.backend.entity.User;
import com.moduflow.backend.entity.UserRole;
import com.moduflow.backend.repository.UserRepository;
import com.moduflow.backend.security.AdminTestAccountProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTestAccountInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void disabledInitializerDoesNothing() {
        AdminTestAccountProperties properties = new AdminTestAccountProperties();
        AdminTestAccountInitializer initializer = new AdminTestAccountInitializer(properties, userRepository, passwordEncoder);

        initializer.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void createsEnabledAdminTestAccount() {
        AdminTestAccountProperties properties = enabledProperties();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("AdminPass123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminTestAccountInitializer initializer = new AdminTestAccountInitializer(properties, userRepository, passwordEncoder);
        initializer.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getName()).isEqualTo("CMS Admin");
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getActive()).isTrue();
    }

    private AdminTestAccountProperties enabledProperties() {
        AdminTestAccountProperties properties = new AdminTestAccountProperties();
        properties.setEnabled(true);
        properties.setEmail(" ADMIN@Example.COM ");
        properties.setPassword("AdminPass123!");
        properties.setName(" CMS Admin ");
        return properties;
    }
}
