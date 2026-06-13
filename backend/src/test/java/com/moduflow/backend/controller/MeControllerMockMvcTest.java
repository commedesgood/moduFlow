package com.moduflow.backend.controller;

import com.moduflow.backend.dto.DeviceRegistrationRequest;
import com.moduflow.backend.dto.DeviceRegistrationResponse;
import com.moduflow.backend.dto.MeProfileResponse;
import com.moduflow.backend.dto.ProfileNameUpdateRequest;
import com.moduflow.backend.exception.CustomException;
import com.moduflow.backend.exception.GlobalExceptionHandler;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.DeviceRegistrationService;
import com.moduflow.backend.service.MeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MeControllerMockMvcTest {

    @Mock
    private MeService meService;

    @Mock
    private DeviceRegistrationService deviceRegistrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MeController(meService, deviceRegistrationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .build();
    }

    @Test
    void getMeReturnsDatabaseProfile() throws Exception {
        when(meService.getMe(1L)).thenReturn(new MeProfileResponse(1L, "user@example.com", "Saved Name"));

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("Saved Name"));
    }

    @Test
    void patchMeUpdatesLoggedInUsersName() throws Exception {
        when(meService.updateName(eq(1L), any(ProfileNameUpdateRequest.class)))
                .thenReturn(new MeProfileResponse(1L, "user@example.com", "새 이름"));

        mockMvc.perform(patch("/api/v1/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("새 이름"));

        verify(meService).updateName(eq(1L), any(ProfileNameUpdateRequest.class));
    }

    @Test
    void patchMeReturnsBadRequestForBlankName() throws Exception {
        when(meService.updateName(eq(1L), any(ProfileNameUpdateRequest.class)))
                .thenThrow(new CustomException(HttpStatus.BAD_REQUEST, "PROFILE_BAD_REQUEST", "name is required."));

        mockMvc.perform(patch("/api/v1/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROFILE_BAD_REQUEST"));
    }

    @Test
    void registerDeviceStoresDeviceForLoggedInUser() throws Exception {
        when(deviceRegistrationService.register(eq(1L), any(DeviceRegistrationRequest.class)))
                .thenReturn(new DeviceRegistrationResponse(1L, "a1b2********g7h8", null));

        mockMvc.perform(post("/api/v1/me/device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "androidId": "a1b2c3d4e5f6g7h8"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.maskedAndroidId").value("a1b2********g7h8"));

        verify(deviceRegistrationService).register(eq(1L), any(DeviceRegistrationRequest.class));
    }

    private static class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(CustomUserDetails.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            return new CustomUserDetails(
                    1L,
                    "user@example.com",
                    "",
                    "Stale Auth Name",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }
    }
}
