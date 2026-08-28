package com.jobportal.entity;

import com.jobportal.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Persistent representation of an application user.
 *
 * <p>Passwords are ALWAYS stored as BCrypt hashes — never in plain text.
 * The {@code password} field is excluded from {@code toString()} via
 * {@link ToString.Exclude} to prevent accidental logging.</p>
 *
 * <p>Auditing timestamps ({@code createdAt}, {@code updatedAt}) are managed
 * automatically by Spring Data JPA auditing — see
 * {@link com.jobportal.JobPortalBackendApplication} for {@code @EnableJpaAuditing}.</p>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full display name — required, max 150 characters. */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Email address — serves as the unique login identifier.
     * Normalised to lowercase before persistence.
     */
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    /**
     * BCrypt-hashed password.
     * Plain-text passwords are NEVER stored here.
     * Column length 72 is sufficient for BCrypt output.
     */
    @Column(nullable = false, length = 72)
    @ToString.Exclude
    private String password;

    /** Application role — stored as a VARCHAR string for readability. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Whether the account is enabled.
     * Defaults to {@code true}; can be set to {@code false} to disable access
     * without deleting the record.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Timestamp of when the record was first persisted. Immutable after creation. */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update to this record. */
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
