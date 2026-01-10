package com.example.incidentplatform.api;

import com.example.incidentplatform.api.dto.login.LoginRequest;
import com.example.incidentplatform.api.dto.login.LoginTokenResponse;
import com.example.incidentplatform.api.dto.refresh.RefreshRequest;
import com.example.incidentplatform.api.dto.refresh.RefreshResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthRefreshFlowIT {

    @Autowired
    TestRestTemplate http;

    @Test
    void refresh_rotates_and_old_token_becomes_invalid() {
        // 1) register user
        var reg = new HttpEntity<>(
                """
                {"email":"it@example.com","displayName":"IT User","password":"P@ssw0rd123"}
                """,
                jsonHeaders()
        );
        http.postForEntity("/api/public/auth/register", reg, String.class);

        // 2) login -> get refresh token
        ResponseEntity<LoginTokenResponse> loginRes = http.postForEntity(
                "/api/public/auth/login",
                new HttpEntity<>(new LoginRequest("it@example.com", "P@ssw0rd123"), jsonHeaders()),
                LoginTokenResponse.class
        );
        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        String oldRefresh = loginRes.getBody().refreshToken();
        assertThat(oldRefresh).isNotBlank();

        // 3) refresh -> new refresh token
        ResponseEntity<RefreshResponse> refreshRes = http.postForEntity(
                "/api/public/auth/refresh",
                new HttpEntity<>(new RefreshRequest(oldRefresh), jsonHeaders()),
                RefreshResponse.class
        );
        assertThat(refreshRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        String newRefresh = refreshRes.getBody().refreshToken();
        assertThat(newRefresh).isNotBlank();
        assertThat(newRefresh).isNotEqualTo(oldRefresh);

        // 4) old refresh token should now fail
        ResponseEntity<String> oldAgain = http.postForEntity(
                "/api/public/auth/refresh",
                new HttpEntity<>(new RefreshRequest(oldRefresh), jsonHeaders()),
                String.class
        );
        assertThat(oldAgain.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return h;
    }
}
