package com.jobportal.controller;

import com.jobportal.dto.ApiResponse;
import com.jobportal.dto.auth.AuthResponse;
import com.jobportal.dto.auth.LoginRequest;
import com.jobportal.dto.auth.RegisterRequest;
import com.jobportal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — registration and login.
 *
 * <p>Both endpoints are publicly accessible (no JWT required).
 * See {@link com.jobportal.security.SecurityConfig} for the permit-all rules.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * <p>Accepted roles: {@code CANDIDATE}, {@code RECRUITER}.
     * {@code ADMIN} registration is rejected with {@code 400 Bad Request}.
     *
     * @param request validated registration details
     * @return 201 Created with user info (no token; user must login separately)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", data));
    }

    /**
     * Authenticate a user and obtain a JWT access token.
     *
     * @param request validated login credentials
     * @return 200 OK with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
    }
}
