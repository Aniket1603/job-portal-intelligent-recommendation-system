package com.jobportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the Spring application context (including Spring Security and JWT)
 * loads successfully. DB and JWT env vars are overridden so this test does not
 * require a live MySQL instance or real JWT_SECRET.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "jwt.secret=test-secret-key-for-unit-tests-only-minimum-32-bytes-ok",
        "jwt.expiration=3600000"
})
class JobPortalBackendApplicationTests {

    @Test
    void contextLoads() {
        // If the context fails to load, this test fails automatically.
    }
}
