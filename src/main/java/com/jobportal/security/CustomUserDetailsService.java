package com.jobportal.security;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads {@link User} entities by email for Spring Security authentication.
 *
 * <p>The {@link Transactional} annotation ensures the entity is fully loaded
 * within a single session, preventing lazy-loading issues when accessing
 * user fields during authentication.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locate the user by email (used as the "username" in this system).
     *
     * @param email the email to look up
     * @return a {@link CustomUserPrincipal} wrapping the found user
     * @throws UsernameNotFoundException if no user with the email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));
        return new CustomUserPrincipal(user);
    }
}
