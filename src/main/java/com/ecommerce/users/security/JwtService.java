package com.ecommerce.users.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.ecommerce.users.model.Role;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    // In real deployment this comes from an environment variable, never hardcoded —
    // fine to hardcode only while you're the sole developer testing locally
    private final SecretKey key = Keys.hmacShaKeyFor(
            "this-is-a-dev-only-secret-key-change-it-before-any-real-deploy".getBytes());

    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hours

    public String generateToken(Long userId, String email, Role role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public String extractRole(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Long extractUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }
}