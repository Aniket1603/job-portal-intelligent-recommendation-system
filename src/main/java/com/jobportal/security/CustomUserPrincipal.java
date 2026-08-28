package com.jobportal.security;

import com.jobportal.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal adapter wrapping the {@link User} entity.
 *
 * <p>Roles are prefixed with {@code ROLE_} to satisfy Spring Security's
 * convention for {@code hasRole()} expressions.</p>
 */
public class CustomUserPrincipal implements UserDetails {

    /** The underlying domain user — accessible for JWT claim population. */
    @Getter
    private final User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    // -------------------------------------------------------------------------
    // Authorities
    // -------------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // -------------------------------------------------------------------------
    // Credentials
    // -------------------------------------------------------------------------

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** Returns the email address as the username (unique login identifier). */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // -------------------------------------------------------------------------
    // Account status — delegated to User.active
    // -------------------------------------------------------------------------

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
