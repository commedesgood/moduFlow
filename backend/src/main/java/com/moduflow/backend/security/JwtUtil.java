package com.moduflow.backend.security;

import com.moduflow.backend.entity.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final Key key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;

        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is missing. Set `security.jwt.secret` in application.yml (min 32 chars).");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret is too short. Use at least 32 bytes for HS256.");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        return generateToken(email, UserRole.USER);
    }

    public String generateToken(String email, UserRole role) {
        UserRole resolvedRole = role == UserRole.ADMIN ? UserRole.ADMIN : UserRole.USER;
        return Jwts.builder()
                .setSubject(email)
                .claim("role", resolvedRole.name())
                .claim("authorities", authoritiesOf(resolvedRole))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + properties.getExpiration().toMillis()))
                .signWith(key)
                .compact();
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private List<String> authoritiesOf(UserRole role) {
        List<String> authorities = new ArrayList<>();
        if (role == UserRole.ADMIN) {
            authorities.add("ROLE_ADMIN");
        }
        authorities.add("ROLE_USER");
        return authorities;
    }
}
