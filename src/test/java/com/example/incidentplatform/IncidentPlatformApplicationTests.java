package com.example.incidentplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        // Temporary: we haven't wired Postgres/Testcontainers yet,
        // so don't require a datasource just to verify Spring wiring starts.
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class IncidentPlatformApplicationTests {

    @Test
    void contextLoads() {
        // If Spring can start the ApplicationContext, this test passes.
    }
}
