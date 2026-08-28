package com.jobportal.controller;

import com.jobportal.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Temporary RBAC verification endpoints.
 *
 * <p><strong>⚠️ TODO — Remove or refactor in Phase 3+.</strong>
 * These endpoints exist solely to verify that Spring Security role-based
 * authorization is working correctly. They contain NO business logic.</p>
 *
 * <p>Access rules (enforced in {@link com.jobportal.security.SecurityConfig}):
 * <ul>
 *   <li>{@code GET /api/test/candidate} — CANDIDATE role only</li>
 *   <li>{@code GET /api/test/recruiter} — RECRUITER role only</li>
 *   <li>{@code GET /api/test/admin}     — ADMIN role only</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * CANDIDATE-only verification endpoint.
     * Returns 200 for CANDIDATE, 403 for any other role, 401 if unauthenticated.
     */
    @GetMapping("/candidate")
    public ResponseEntity<ApiResponse<Void>> candidateOnly(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Candidate access granted for: " + principal.getUsername()));
    }

    /**
     * RECRUITER-only verification endpoint.
     * Returns 200 for RECRUITER, 403 for any other role, 401 if unauthenticated.
     */
    @GetMapping("/recruiter")
    public ResponseEntity<ApiResponse<Void>> recruiterOnly(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Recruiter access granted for: " + principal.getUsername()));
    }

    /**
     * ADMIN-only verification endpoint.
     * Returns 200 for ADMIN, 403 for any other role, 401 if unauthenticated.
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Void>> adminOnly(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.ok("Admin access granted for: " + principal.getUsername()));
    }
}
