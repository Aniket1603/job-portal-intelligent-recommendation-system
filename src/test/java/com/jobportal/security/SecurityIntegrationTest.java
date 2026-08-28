package com.jobportal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.dto.auth.LoginRequest;
import com.jobportal.dto.auth.RegisterRequest;
import com.jobportal.entity.User;
import com.jobportal.enums.Role;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Spring Security authorization rules.
 *
 * <p>Tests 10–16 from the Phase 2 specification:
 * <ol start="10">
 *   <li>Missing token → 401</li>
 *   <li>Invalid token → 401</li>
 *   <li>Candidate token → /api/test/candidate → 200</li>
 *   <li>Candidate token → /api/test/recruiter → 403</li>
 *   <li>Recruiter token → /api/test/recruiter → 200</li>
 *   <li>Admin token → /api/test/admin → 200</li>
 *   <li>Candidate token → /api/test/admin → 403</li>
 * </ol>
 *
 * <p>Uses a full Spring Boot context with H2. Tokens are obtained via real
 * login calls to ensure end-to-end coverage of the JWT flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:sectest;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "jwt.secret=test-secret-key-for-unit-tests-only-minimum-32-bytes-ok",
        "jwt.expiration=3600000"
})
class SecurityIntegrationTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService   jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String candidateToken;
    private String recruiterToken;
    private String adminToken;       // seeded manually via service in setup

    // =========================================================================
    // Setup — register users and obtain tokens
    // =========================================================================

    @BeforeEach
    void setUp() throws Exception {
        candidateToken = registerAndLogin("sec_candidate@test.com", "Password123!", Role.CANDIDATE);
        recruiterToken = registerAndLogin("sec_recruiter@test.com", "Password456!", Role.RECRUITER);
        // ADMIN cannot self-register — save directly to DB then generate token
        adminToken = createAdminTokenAndSaveUser();
    }

    // =========================================================================
    // Tests 10–11 — Unauthenticated / invalid token
    // =========================================================================

    @Test
    @DisplayName("10. No token → protected endpoint returns 401")
    void noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/test/candidate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("11. Invalid token → protected endpoint returns 401")
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/test/candidate")
                        .header("Authorization", "Bearer this.is.an.invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Tests 12–13 — CANDIDATE access
    // =========================================================================

    @Test
    @DisplayName("12. Candidate token → /api/test/candidate → 200 OK")
    void candidateToken_candidateEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/test/candidate")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("13. Candidate token → /api/test/recruiter → 403 Forbidden")
    void candidateToken_recruiterEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/test/recruiter")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // Test 14 — RECRUITER access
    // =========================================================================

    @Test
    @DisplayName("14. Recruiter token → /api/test/recruiter → 200 OK")
    void recruiterToken_recruiterEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/test/recruiter")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // Tests 15–16 — ADMIN access
    // =========================================================================

    @Test
    @DisplayName("15. Admin token → /api/test/admin → 200 OK")
    void adminToken_adminEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("16. Candidate token → /api/test/admin → 403 Forbidden")
    void candidateToken_adminEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // Additional cross-role checks
    // =========================================================================

    @Test
    @DisplayName("Recruiter token → /api/test/candidate → 403 Forbidden")
    void recruiterToken_candidateEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/test/candidate")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Recruiter token → /api/test/admin → 403 Forbidden")
    void recruiterToken_adminEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Register a user then log in to obtain a real JWT.
     * Used for CANDIDATE and RECRUITER — ADMIN cannot self-register.
     */
    private String registerAndLogin(String email, String password, Role role) throws Exception {
        // Register (ignore if already exists from a previous test run)
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Test User");
        reg.setEmail(email);
        reg.setPassword(password);
        reg.setRole(role);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Login
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
    }

    /**
     * Save an ADMIN user directly to the database (bypassing public registration),
     * then generate a valid JWT for that user.
     *
     * <p>The user MUST exist in the database so that {@link JwtAuthenticationFilter}
     * can call {@code loadUserByUsername} successfully when the token is presented.</p>
     */
    private String createAdminTokenAndSaveUser() {
        User adminUser = User.builder()
                .name("Test Admin")
                .email("sec_admin@test.com")
                .password(passwordEncoder.encode("AdminPass!"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        User saved = userRepository.save(adminUser);
        return jwtService.generateToken(new CustomUserPrincipal(saved));
    }
}
