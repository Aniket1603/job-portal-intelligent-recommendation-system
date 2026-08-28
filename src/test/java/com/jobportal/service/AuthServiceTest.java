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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Covers:
 * <ol>
 *   <li>Registration success</li>
 *   <li>Duplicate email → DuplicateResourceException</li>
 *   <li>ADMIN role blocked → BadRequestException</li>
 *   <li>Password BCrypt hashed (not stored plain-text)</li>
 *   <li>Login success → JWT returned</li>
 *   <li>Invalid credentials → BadCredentialsException</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService            jwtService;

    @InjectMocks
    private AuthService authService;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private RegisterRequest candidateRequest;
    private RegisterRequest recruiterRequest;
    private RegisterRequest adminRequest;
    private LoginRequest    loginRequest;
    private User            savedUser;

    @BeforeEach
    void setUp() {
        candidateRequest = new RegisterRequest();
        candidateRequest.setName("Alice Smith");
        candidateRequest.setEmail("alice@example.com");
        candidateRequest.setPassword("Password123!");
        candidateRequest.setRole(Role.CANDIDATE);

        recruiterRequest = new RegisterRequest();
        recruiterRequest.setName("Bob Jones");
        recruiterRequest.setEmail("bob@example.com");
        recruiterRequest.setPassword("Passw0rd456!");
        recruiterRequest.setRole(Role.RECRUITER);

        adminRequest = new RegisterRequest();
        adminRequest.setName("Admin User");
        adminRequest.setEmail("admin@example.com");
        adminRequest.setPassword("AdminPass789!");
        adminRequest.setRole(Role.ADMIN);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("Password123!");

        savedUser = User.builder()
                .id(1L)
                .name("Alice Smith")
                .email("alice@example.com")
                .password("$2a$10$hashedpassword")
                .role(Role.CANDIDATE)
                .active(true)
                .build();
    }

    // =========================================================================
    // Registration tests
    // =========================================================================

    @Test
    @DisplayName("1. Register CANDIDATE — success")
    void register_success_candidate() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(candidateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Smith");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo(Role.CANDIDATE);
        assertThat(response.getAccessToken()).isNull(); // no token on registration

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    @DisplayName("2. Register — duplicate email throws DuplicateResourceException")
    void register_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(candidateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("alice@example.com");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("3. Register ADMIN — blocked with BadRequestException")
    void register_adminRole_throwsBadRequestException() {
        assertThatThrownBy(() -> authService.register(adminRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN");

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("4. Register — password is BCrypt hashed, not stored plain-text")
    void register_passwordIsBcryptHashed() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$bcrypthash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Verify the saved entity has the hashed password, not plain-text
            assertThat(user.getPassword()).isEqualTo("$2a$10$bcrypthash");
            assertThat(user.getPassword()).doesNotContain("Password123!");
            return savedUser;
        });

        authService.register(candidateRequest);

        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    @DisplayName("4b. Register — email is normalised to lowercase")
    void register_emailIsNormalised() {
        RegisterRequest mixed = new RegisterRequest();
        mixed.setName("Mixed Case");
        mixed.setEmail("  ALICE@EXAMPLE.COM  ");
        mixed.setPassword("Password123!");
        mixed.setRole(Role.CANDIDATE);

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getEmail()).isEqualTo("alice@example.com");
            return savedUser;
        });

        authService.register(mixed);
    }

    // =========================================================================
    // Login tests
    // =========================================================================

    @Test
    @DisplayName("5. Login — success, JWT returned")
    void login_success_returnsJwt() {
        CustomUserPrincipal principal = new CustomUserPrincipal(savedUser);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateToken(principal)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getRole()).isEqualTo(Role.CANDIDATE);
        assertThat(response.getEmail()).isEqualTo("alice@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(principal);
    }

    @Test
    @DisplayName("6. Login — invalid password throws BadCredentialsException")
    void login_invalidPassword_throwsBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
