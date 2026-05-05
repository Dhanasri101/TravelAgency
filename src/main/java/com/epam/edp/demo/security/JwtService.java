package com.epam.edp.demo.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.ttl-hours:24}") long ttlHours
    ) {
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException ignore) {
            raw = base64Secret.getBytes(StandardCharsets.UTF_8);
        }
        if (raw.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must decode to at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(raw);
        this.ttl = Duration.ofHours(ttlHours);
    }

    public IssuedToken issue(String userId, String email, String firstName) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        String token = Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email, "firstName", firstName))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new IssuedToken(token, exp);
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException(e.getMessage());
        }
    }

    public record IssuedToken(String token, Instant expiresAt) {}

    public static class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String msg) { super(msg); }
    }
}

