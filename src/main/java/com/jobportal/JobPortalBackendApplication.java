package com.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * <p>JPA auditing ({@code @EnableJpaAuditing}) is configured in
 * {@link com.jobportal.config.JpaConfig} to allow {@code @WebMvcTest}
 * slices to load without the JPA layer.</p>
 */
@SpringBootApplication
public class JobPortalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobPortalBackendApplication.class, args);
    }

}
