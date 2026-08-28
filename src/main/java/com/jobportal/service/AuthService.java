package com.jobportal.service;

import com.jobportal.dto.auth.AuthResponse;
import com.jobportal.dto.auth.LoginRequest;
import com.jobportal.dto.auth.RegisterRequest;
import com.jobportal.entity.User;
import com.jobportal.enums.Role;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.DuplicateResourceException;
import com.jobportal.repository.UserRepository;
import com.jobportal.security.CustomUserPrincipal;
import com.jobportal.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service — registration and login.
 *
 * <p>Security invariants enforced here:
 * <ul>
 *   <li>Passwords are NEVER stored in plain text — always BCrypt hashed.</li>
 *   <li>Passwords are NEVER logged.</li>
 *   <li>Passwords are NEVER returned in responses.</li>
 *   <li>Public registration of the {@link Role#ADMIN} role is blocked.</li>
 *   <li>Email is normalised (trimmed + lowercase) before any lookup or persist.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService            jwtService;

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Register a new user.
     *
     * <p>Steps:
     * <ol>
     *   <li>Block ADMIN self-registration.</li>
     *   <li>Normalise the email (trim + lowercase).</li>
     *   <li>Reject duplicate emails (409 Conflict).</li>
     *   <li>BCrypt hash the password.</li>
     *   <li>Persist the user.</li>
     *   <li>Return safe user information (no token, no password hash).</li>
     * </ol>
     *
     * @param request validated registration request
     * @return {@link AuthResponse} with user info only (no token)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Block ADMIN self-registration — ADMIN accounts must be seeded
        if (Role.ADMIN == request.getRole()) {
            throw new BadRequestException(
                    "ADMIN accounts cannot be created through public registration");
        }

        String normalisedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalisedEmail)) {
            throw new DuplicateResourceException("User", "email", normalisedEmail);
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(normalisedEmail)
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .role(request.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user [id={}, role={}]", saved.getId(), saved.getRole());

        return buildAuthResponse(saved, null);
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /**
     * Authenticate a user and return a JWT.
     *
     * <p>Delegates credential verification to {@link AuthenticationManager}
     * which uses {@link com.jobportal.security.CustomUserDetailsService} and
     * BCrypt comparison internally. On success a JWT is generated and returned.
     *
     * @param request validated login request
     * @return {@link AuthResponse} with JWT access token and user info
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalisedEmail = request.getEmail().trim().toLowerCase();

        // AuthenticationManager handles BCrypt verification and throws on failure
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalisedEmail,
                        request.getPassword()  // plain-text, NOT logged
                )
        );

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        log.info("User authenticated [id={}, role={}]",
                principal.getUser().getId(), principal.getUser().getRole());

        return buildAuthResponse(principal.getUser(), token);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Build an {@link AuthResponse} from a {@link User} entity.
     * The password hash is intentionally excluded.
     *
     * @param user  the persisted user
     * @param token JWT string, or {@code null} for registration responses
     */
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
