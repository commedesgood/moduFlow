package com.fitflow.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtFilterTest {

    private final JwtFilter jwtFilter = new JwtFilter(
            mock(JwtUtil.class),
            mock(CustomUserDetailsService.class)
    );

    @Test
    void shouldSkipOAuthAuthorizationAndCallbackPaths() {
        assertThat(jwtFilter.shouldNotFilter(request("GET", "/oauth2/authorization/google"))).isTrue();
        assertThat(jwtFilter.shouldNotFilter(request("GET", "/oauth2/authorization/kakao"))).isTrue();
        assertThat(jwtFilter.shouldNotFilter(request("GET", "/oauth2/authorization/naver"))).isTrue();
        assertThat(jwtFilter.shouldNotFilter(request("GET", "/login/oauth2/code/google"))).isTrue();
    }

    @Test
    void shouldSkipLoginAndSignupPaths() {
        assertThat(jwtFilter.shouldNotFilter(request("POST", "/api/v1/auth/login"))).isTrue();
        assertThat(jwtFilter.shouldNotFilter(request("POST", "/api/v1/auth/signup"))).isTrue();
        assertThat(jwtFilter.shouldNotFilter(request("POST", "/auth/login"))).isTrue();
        assertThat(jwtFilter.shouldNotFilter(request("POST", "/auth/signup"))).isTrue();
    }

    @Test
    void shouldFilterProtectedApiPaths() {
        assertThat(jwtFilter.shouldNotFilter(request("GET", "/api/v1/workouts"))).isFalse();
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
