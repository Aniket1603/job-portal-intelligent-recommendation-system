package com.jobportal.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jobportal.enums.Role;
import lombok.Builder;
import lombok.Getter;

/**
 * Response payload returned after a successful login.
 *
 * <p>Contains the JWT access token and enough user information for the
 * client to bootstrap the UI without an extra round-trip.</p>
 *
 * <p><strong>The password hash is NEVER included in this response.</strong></p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /** JWT access token. Only present on a login response. */
    private final String accessToken;

    /** Token type — always {@code "Bearer"}. */
    @Builder.Default
    private final String tokenType = "Bearer";

    /** Database ID of the authenticated user. */
    private final Long id;

    /** Display name of the authenticated user. */
    private final String name;

    /** Email address of the authenticated user. */
    private final String email;

    /** Role of the authenticated user. */
    private final Role role;
}
