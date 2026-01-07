package com.example.incidentplatform.infrastructure.security;

import com.example.incidentplatform.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenService {

    private final String issuer;
    private final long accessTokenTtlSeconds;
    private final SecretKey key;

    public JwtTokenService(
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
            @Value("${security.jwt.secret}") String secret
    ) {
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenTtlSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.id().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", user.email())
                .signWith(key)
                .compact();


    }
}
