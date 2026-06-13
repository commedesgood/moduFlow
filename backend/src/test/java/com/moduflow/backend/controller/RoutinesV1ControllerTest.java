package com.moduflow.backend.controller;

import com.moduflow.backend.dto.RoutineScheduleDto;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.RoutinesV1Service;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutinesV1ControllerTest {

    @Mock
    private RoutinesV1Service routinesV1Service;

    @Test
    void getUsesAuthenticatedUserId() {
        RoutinesV1Controller controller = new RoutinesV1Controller(routinesV1Service);
        CustomUserDetails userDetails = new CustomUserDetails(
                42L,
                "member@example.com",
                "",
                "Member",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        RoutineScheduleDto schedule = new RoutineScheduleDto(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        when(routinesV1Service.getRoutines(42L)).thenReturn(schedule);

        RoutineScheduleDto response = controller.get(userDetails);

        assertThat(response).isSameAs(schedule);
        verify(routinesV1Service).getRoutines(42L);
    }
}
