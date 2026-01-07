package com.example.incidentplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IncidentPlatformApplicationTests {

    @Test
    void contextLoads() {
        // If Spring can start the ApplicationContext, this test passes.
    }
}
