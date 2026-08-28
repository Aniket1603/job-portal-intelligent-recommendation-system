package com.jobportal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Spring Security configuration for Phase 2.
 *
 * <p>Uses {@link SecurityFilterChain} (no deprecated {@code WebSecurityConfigurerAdapter}).
 * Sessions are fully stateless — authentication state lives only in the JWT.
 *
 * <p>Access rules:
 * <ul>
 *   <li>Public: {@code POST /api/auth/**}, {@code GET /api/health}</li>
 *   <li>CANDIDATE only: {@code /api/candidate/**}, {@code GET /api/test/candidate}</li>
 *   <li>RECRUITER only: {@code /api/recruiter/**}, {@code GET /api/test/recruiter}</li>
 *   <li>ADMIN only:     {@code /api/admin/**},    {@code GET /api/test/admin}</li>
 *   <li>All other endpoints: require authentication</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter  jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper             objectMapper;

    // -------------------------------------------------------------------------
    // Security filter chain
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Disable form login and HTTP basic
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Stateless session — no HttpSession created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/health").permitAll()

                        // Role-based route prefixes (future business controllers)
                        .requestMatchers("/api/candidate/**").hasRole("CANDIDATE")
                        .requestMatchers("/api/recruiter/**").hasRole("RECRUITER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Temporary RBAC verification endpoints — remove/refactor in Phase 3+
                        .requestMatchers(HttpMethod.GET, "/api/test/candidate").hasRole("CANDIDATE")
                        .requestMatchers(HttpMethod.GET, "/api/test/recruiter").hasRole("RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/test/admin").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Custom 401 / 403 handlers returning JSON consistent with ErrorResponse
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                )

                // Register the JWT filter before the standard username/password filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Authentication provider
    // -------------------------------------------------------------------------

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // -------------------------------------------------------------------------
    // Password encoder
    // -------------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -------------------------------------------------------------------------
    // Custom entry points — JSON error responses consistent with ErrorResponse
    // -------------------------------------------------------------------------

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) -> {
            log.debug("Unauthorized access to [{}]: {}", request.getRequestURI(), authException.getMessage());
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    "Authentication required", request.getRequestURI());
        };
    }

    @Bean
    public AccessDeniedHandler forbiddenHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                AccessDeniedException accessDeniedException) -> {
            log.debug("Access denied to [{}]: {}", request.getRequestURI(), accessDeniedException.getMessage());
            writeErrorResponse(response, HttpStatus.FORBIDDEN,
                    "Access denied — insufficient permissions", request.getRequestURI());
        };
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void writeErrorResponse(HttpServletResponse response,
                                    HttpStatus status,
                                    String message,
                                    String path) throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now())
                .path(path)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
