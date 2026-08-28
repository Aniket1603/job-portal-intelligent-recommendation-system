package com.jobportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA auditing configuration.
 *
 * <p>Placed in a dedicated {@code @Configuration} class (instead of
 * directly on {@link com.jobportal.JobPortalBackendApplication}) so that
 * {@code @WebMvcTest} slices — which do not load the JPA layer — can
 * exclude it automatically. Without this separation,
 * {@code @WebMvcTest} contexts fail with
 * "Cannot resolve reference to bean 'jpaMappingContext'".</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
