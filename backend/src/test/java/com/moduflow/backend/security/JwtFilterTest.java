package com.moduflow.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtFilterTest {

    private final JwtFilter jwtFilter = new JwtFilter(mock(JwtUtil.class), mock(CustomUserDetailsService.class));

    @Test
    void shouldNotFilterVersionedBeaconPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/update-location");
        request.setServletPath("/api/v1/update-location");

        assertThat(jwtFilter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldFilterAuthenticatedRoutinePaths() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/routines");
        request.setServletPath("/api/v1/routines");

        assertThat(jwtFilter.shouldNotFilter(request)).isFalse();
    }
}
