package com.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.dto.auth.LoginRequest;
import com.jobportal.dto.auth.RegisterRequest;
import com.jobportal.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 *
 * <p>Uses a full Spring Boot context with H2 in-memory database.
 * Covers registration and login end-to-end without mocking the service layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:authtest;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "jwt.secret=test-secret-key-for-unit-tests-only-minimum-32-bytes-ok",
        "jwt.expiration=3600000"
})
class AuthControllerIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    // =========================================================================
    // Registration tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/auth/register — CANDIDATE → 201 Created")
    void register_candidate_returns201() throws Exception {
        RegisterRequest req = registerRequest("Carol Test", "carol@test.com",
                "Password123!", Role.CANDIDATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.email").value("carol@test.com"))
                .andExpect(jsonPath("$.data.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.data.password").doesNotExist()); // NEVER expose password
    }

    @Test
    @DisplayName("POST /api/auth/register — RECRUITER → 201 Created")
    void register_recruiter_returns201() throws Exception {
        RegisterRequest req = registerRequest("Dave Test", "dave@test.com",
                "Password456!", Role.RECRUITER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("RECRUITER"));
    }

    @Test
    @DisplayName("POST /api/auth/register — duplicate email → 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = registerRequest("Eve Test", "eve@test.com",
                "Password789!", Role.CANDIDATE);

        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register — ADMIN role → 400 Bad Request")
    void register_adminRole_returns400() throws Exception {
        RegisterRequest req = registerRequest("Admin Test", "admin@test.com",
                "AdminPass!", Role.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register — invalid payload (missing name) → 400")
    void register_invalidPayload_returns400() throws Exception {
        RegisterRequest req = registerRequest("", "bad@test.com", "Password!", Role.CANDIDATE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").isNotEmpty());
    }

    // =========================================================================
    // Login tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/auth/login — valid credentials → 200 with JWT")
    void login_validCredentials_returnsJwt() throws Exception {
        // Register first
        RegisterRequest reg = registerRequest("Frank Test", "frank@test.com",
                "Password123!", Role.CANDIDATE);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest login = new LoginRequest();
        login.setEmail("frank@test.com");
        login.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andReturn();

        String token = objectMapper.readTree(
                result.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // valid JWT structure
    }

    @Test
    @DisplayName("POST /api/auth/login — wrong password → 401")
    void login_wrongPassword_returns401() throws Exception {
        // Register
        RegisterRequest reg = registerRequest("Grace Test", "grace@test.com",
                "Password123!", Role.CANDIDATE);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Login with wrong password
        LoginRequest login = new LoginRequest();
        login.setEmail("grace@test.com");
        login.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login — non-existent user → 401")
    void login_nonExistentUser_returns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("nobody@test.com");
        login.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private RegisterRequest registerRequest(String name, String email,
                                            String password, Role role) {
        RegisterRequest req = new RegisterRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPassword(password);
        req.setRole(role);
        return req;
    }
}
