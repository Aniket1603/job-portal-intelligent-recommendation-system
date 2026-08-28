package com.jobportal.dto.auth;

import com.jobportal.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for {@code POST /api/auth/register}.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>name — required, 1–150 chars</li>
 *   <li>email — required, valid format, max 254 chars (RFC 5321)</li>
 *   <li>password — required, 8–128 chars (supports passphrases)</li>
 *   <li>role — required; must be a valid {@link Role} value</li>
 * </ul>
 *
 * <p>Note: the {@link Role#ADMIN} value is accepted at the DTO level so
 * the validation layer can produce a clear error. The service layer enforces
 * the business rule that {@code ADMIN} cannot self-register publicly.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}
