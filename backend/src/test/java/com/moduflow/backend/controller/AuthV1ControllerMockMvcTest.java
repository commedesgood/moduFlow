package com.moduflow.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moduflow.backend.dto.AuthResponse;
import com.moduflow.backend.dto.UserInfoResponse;
import com.moduflow.backend.exception.GlobalExceptionHandler;
import com.moduflow.backend.service.AuthV1Service;
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
    void loginRejectsMissingCredentialsBeforeServiceCall() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
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
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));
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

    @Test
    void loginAcceptsDeviceIdWithEmailPasswordPayload() throws Exception {
        AuthResponse response = new AuthResponse(
                "access-token",
                new UserInfoResponse("u_7", "member@example.com", "Member")
        );
        when(authV1Service.login(argThat(request ->
                "member@example.com".equals(request.getEmail())
                        && "password123".equals(request.getPassword())
                        && "a1b2c3d4e5f6g7h8".equals(request.getUserId())
        )))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "member@example.com",
                                  "password": "password123",
                                  "userId": "a1b2c3d4e5f6g7h8"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.name").value("Member"));
    }

    @Test
    void googleLoginAcceptsAndroidIdTokenPayload() throws Exception {
        AuthResponse response = new AuthResponse(
                "access-token",
                new UserInfoResponse("u_9", "google@example.com", "Google User")
        );
        when(authV1Service.loginWithGoogleIdToken(argThat(request ->
                "id-token".equals(request.getIdToken())
                        && "a1b2c3d4e5f6g7h8".equals(request.getUserId())
        ))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_token": "id-token",
                                  "deviceId": "a1b2c3d4e5f6g7h8"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.email").value("google@example.com"));
    }

    @Test
    void signupAcceptsDisplayName() throws Exception {
        AuthResponse response = new AuthResponse(
                "access-token",
                new UserInfoResponse("u_3", "new@example.com", "홍길동")
        );
        when(authV1Service.signup(argThat(request ->
                "new@example.com".equals(request.getEmail())
                        && "password123".equals(request.getPassword())
                        && "홍길동".equals(request.getName())
        ))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "password123",
                                  "name": "홍길동"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.name").value("홍길동"));
    }

    private record LoginBody(String email, String password) {
    }
}
