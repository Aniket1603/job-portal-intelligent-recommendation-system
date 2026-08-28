package com.jobportal.repository;

import com.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Lookup by email is the primary authentication mechanism.
 * All email values stored in the database are normalised to lowercase.</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     *
     * @param email the normalised (lowercase) email to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether a user with the given email already exists.
     * Preferred over {@code findByEmail} when only existence matters,
     * as it avoids loading the full entity.
     *
     * @param email the normalised (lowercase) email to check
     * @return {@code true} if a user with this email exists
     */
    boolean existsByEmail(String email);
}
