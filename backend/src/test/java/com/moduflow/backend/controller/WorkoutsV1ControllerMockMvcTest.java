package com.moduflow.backend.controller;

import com.moduflow.backend.exception.GlobalExceptionHandler;
import com.moduflow.backend.security.CustomUserDetails;
import com.moduflow.backend.service.WorkoutsV1Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkoutsV1ControllerMockMvcTest {

    @Mock
    private WorkoutsV1Service workoutsV1Service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new WorkoutsV1Controller(workoutsV1Service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void putDayRejectsInvalidWorkoutItemBeforeServiceCall() throws Exception {
        mockMvc.perform(put("/api/v1/workouts/2026-05-14")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "name": "",
                                      "sets": -1,
                                      "reps": -1,
                                      "weight": -10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(workoutsV1Service);
    }

    @Test
    void patchItemRejectsInvalidNumbersBeforeServiceCall() throws Exception {
        mockMvc.perform(patch("/api/v1/workouts/2026-05-14/items/item-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sets": -1,
                                  "reps": 1000,
                                  "weight": -1
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(workoutsV1Service);
    }

    @Test
    void getBetweenRejectsInvalidDateRangeBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/v1/workouts")
                        .param("from", "2026-05-31")
                        .param("to", "2026-05-01"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("from은 to보다 이후일 수 없습니다."));

        verifyNoInteractions(workoutsV1Service);
    }

    @Test
    void getBetweenRejectsMissingRequiredQueryBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/v1/workouts")
                        .param("from", "2026-05-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("to is required."));

        verifyNoInteractions(workoutsV1Service);
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
                    7L,
                    "user@example.com",
                    "",
                    "User",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }
    }
}
