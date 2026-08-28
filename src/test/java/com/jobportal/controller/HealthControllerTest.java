package com.jobportal.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.jobportal.security.SecurityConfig;
import com.jobportal.security.JwtService;
import com.jobportal.security.CustomUserDetailsService;
import com.jobportal.security.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link HealthController}.
 * Does NOT start a full application context and does NOT require a database.
 * Spring Security is active via {@link SecurityConfig} import.
 * The health endpoint is publicly permitted so no token is needed.
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-unit-tests-only-minimum-32-bytes-ok",
        "jwt.expiration=3600000"
})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void health_returnsSuccessResponse() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job Portal Backend is running"));
    }

    @Test
    @WithMockUser
    void health_returnsSuccessResponse_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
