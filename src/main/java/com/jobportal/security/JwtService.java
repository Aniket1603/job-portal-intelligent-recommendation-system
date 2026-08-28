package com.jobportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT generation and validation service.
 *
 * <p>Uses HMAC-SHA-256 (HS256) with a secret key sourced exclusively from
 * the {@code JWT_SECRET} environment variable — never hardcoded.
 *
 * <p>Token claims:
 * <ul>
 *   <li>{@code sub} — user email (unique login identifier)</li>
 *   <li>{@code userId} — database primary key</li>
 *   <li>{@code role} — user role string (e.g., "CANDIDATE")</li>
 * </ul>
 *
 * <p><strong>Security note:</strong> The secret is NEVER logged or returned in
 * any API response.</p>
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Constructs the service.
     *
     * @param secret       raw secret string from {@code JWT_SECRET} env var;
     *                     should be at least 256 bits (32 bytes) for HS256
     * @param expirationMs token lifetime in milliseconds from {@code JWT_EXPIRATION}
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    /**
     * Generate a signed JWT for the given principal.
     *
     * @param principal the authenticated user principal
     * @return compact, URL-safe JWT string
     */
    public String generateToken(CustomUserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(principal.getUsername())                           // email
                .claims(Map.of(
                        "userId", principal.getUser().getId(),
                        "role",   principal.getUser().getRole().name()
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // -------------------------------------------------------------------------
    // Token validation & claims extraction
    // -------------------------------------------------------------------------

    /**
     * Extract the email (subject) from a token without full validation.
     * Use {@link #validateToken(String)} first to confirm the token is valid.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Fully validate a JWT: signature, structure, and expiration.
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is valid and unexpired
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            log.debug("JWT token is unsupported");
        } catch (MalformedJwtException e) {
            log.debug("JWT token is malformed");
        } catch (SecurityException e) {
            // io.jsonwebtoken.security.SecurityException — bad signature
            log.debug("JWT signature validation failed");
        } catch (IllegalArgumentException e) {
            log.debug("JWT token is empty or null");
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
