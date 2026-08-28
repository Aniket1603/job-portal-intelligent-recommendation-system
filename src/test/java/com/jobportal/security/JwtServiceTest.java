package com.jobportal.security;

import com.jobportal.entity.User;
import com.jobportal.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 *
 * <p>Covers:
 * <ol>
 *   <li>Token generation — valid compact JWT produced</li>
 *   <li>Token validation — valid token returns true</li>
 *   <li>Email extraction — subject matches user email</li>
 *   <li>Expired token — validateToken returns false (no exception to caller)</li>
 *   <li>Invalid signature — validateToken returns false</li>
 *   <li>Malformed token — validateToken returns false</li>
 *   <li>Null / empty token — validateToken returns false</li>
 * </ol>
 */
class JwtServiceTest {

    private static final String TEST_SECRET     =
            "test-secret-key-for-unit-tests-only-minimum-32-bytes-ok";
    private static final long   EXPIRATION_MS   = 3_600_000L; // 1 hour
    private static final long   EXPIRED_MS      = 1L;         // expires immediately

    private JwtService jwtService;
    private JwtService expiredJwtService;

    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {
        jwtService        = new JwtService(TEST_SECRET, EXPIRATION_MS);
        expiredJwtService = new JwtService(TEST_SECRET, EXPIRED_MS);

        User user = User.builder()
                .id(42L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$10$hash")
                .role(Role.CANDIDATE)
                .active(true)
                .build();

        principal = new CustomUserPrincipal(user);
    }

    // =========================================================================
    // Tests 7 & 8 — Token generation and validation
    // =========================================================================

    @Test
    @DisplayName("7. JWT generated — compact token string produced")
    void generateToken_producesCompactJwt() {
        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        // JWT format: three Base64-encoded segments separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("8a. JWT validation — valid token returns true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtService.generateToken(principal);
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("8b. JWT email extraction — subject matches user email")
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateToken(principal);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    // =========================================================================
    // Test 9 — Expired token
    // =========================================================================

    @Test
    @DisplayName("9. Expired JWT — validateToken returns false (no exception thrown to caller)")
    void validateToken_expiredToken_returnsFalse() throws InterruptedException {
        String token = expiredJwtService.generateToken(principal);
        // Give the 1ms token time to expire
        Thread.sleep(10);
        assertThat(jwtService.validateToken(token)).isFalse();
    }

    // =========================================================================
    // Additional validation edge cases
    // =========================================================================

    @Test
    @DisplayName("JWT — wrong signature returns false")
    void validateToken_wrongSignature_returnsFalse() {
        JwtService differentKeyService = new JwtService(
                "a-completely-different-secret-key-also-32-bytes-min", EXPIRATION_MS);
        String tokenFromDifferentKey = differentKeyService.generateToken(principal);

        assertThat(jwtService.validateToken(tokenFromDifferentKey)).isFalse();
    }

    @Test
    @DisplayName("JWT — malformed token string returns false")
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtService.validateToken("this.is.not.a.valid.jwt")).isFalse();
    }

    @Test
    @DisplayName("JWT — null token returns false")
    void validateToken_nullToken_returnsFalse() {
        assertThat(jwtService.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("JWT — empty string returns false")
    void validateToken_emptyToken_returnsFalse() {
        assertThat(jwtService.validateToken("")).isFalse();
    }
}
