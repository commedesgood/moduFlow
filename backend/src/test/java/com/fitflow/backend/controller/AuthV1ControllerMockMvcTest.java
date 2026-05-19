package com.fitflow.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitflow.backend.dto.AuthResponse;
import com.fitflow.backend.dto.UserInfoResponse;
import com.fitflow.backend.exception.GlobalExceptionHandler;
import com.fitflow.backend.service.AuthV1Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthV1ControllerMockMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthV1Service authV1Service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthV1Controller(authV1Service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void loginRejectsInvalidEmailBeforeServiceCall() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-email",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(authV1Service);
    }

    @Test
    void loginReturnsAccessTokenContract() throws Exception {
        AuthResponse response = new AuthResponse(
                "access-token",
                new UserInfoResponse("u_1", "user@example.com", "User")
        );
        when(authV1Service.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody("user@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void loginAcceptsLegacyFieldAliasesOnV1Endpoint() throws Exception {
        AuthResponse response = new AuthResponse(
                "access-token",
                new UserInfoResponse("u_1", "user@example.com", "User")
        );
        when(authV1Service.login(argThat(request ->
                "user@example.com".equals(request.getEmail())
                        && "password123".equals(request.getPassword())
        ))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "user@example.com",
                                  "pw": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    private record LoginBody(String email, String password) {
    }
}
