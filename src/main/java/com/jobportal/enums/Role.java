package com.jobportal.enums;

/**
 * Application roles.
 *
 * <ul>
 *   <li>{@link #CANDIDATE} — job seeker</li>
 *   <li>{@link #RECRUITER} — employer / hiring manager</li>
 *   <li>{@link #ADMIN}     — platform administrator (not publicly registrable)</li>
 * </ul>
 *
 * <p>Stored as a {@code VARCHAR} in the database via
 * {@code @Enumerated(EnumType.STRING)} on {@link com.jobportal.entity.User}.</p>
 */
public enum Role {
    CANDIDATE,
    RECRUITER,
    ADMIN
}
