package com.moduflow.backend.security;

import com.moduflow.backend.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-jwt-secret-for-moduflow-admin-role-claim";

    @Test
    void adminTokenContainsRoleAndAuthoritiesClaims() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpiration(Duration.ofHours(1));
        JwtUtil jwtUtil = new JwtUtil(properties);

        String token = jwtUtil.generateToken("admin@example.com", UserRole.ADMIN);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("admin@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("authorities", List.class)).contains("ROLE_ADMIN", "ROLE_USER");
    }
}
