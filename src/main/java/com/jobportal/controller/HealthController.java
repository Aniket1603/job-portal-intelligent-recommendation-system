package com.jobportal.controller;

import com.jobportal.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health-check endpoint.
 * Confirms the application context has started and is accepting requests.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     *
     * @return 200 OK with a JSON body confirming the service is running
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Void>> health() {
        return ResponseEntity.ok(ApiResponse.ok("Job Portal Backend is running"));
    }
}
