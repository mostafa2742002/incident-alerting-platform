package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.RefreshTokenRepository;
import com.example.incidentplatform.common.error.UnauthorizedException;
import com.example.incidentplatform.domain.model.RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // Simple default for now (we can move to application.properties later)
    private static final long REFRESH_TOKEN_TTL_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Create + store refresh token; return RAW token to the client (only time they see it).
     * DB stores only the hash.
     */
    public String issueToken(UUID userId) {
        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);

        // Domain logic uses Instant (timezone-independent)
        Instant expiresAt = Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS);

        RefreshToken refreshToken = RefreshToken.createNew(userId, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    /**
     * Rotate refresh token (safe default):
     * - token must exist and be valid
     * - revoke all tokens for the user
     * - issue a new one
     */
    public record RotationResult(UUID userId, String newRefreshToken) {}

    @Transactional
    public RotationResult rotate(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("invalid refresh token"));

        if (existing.isRevoked() || existing.isExpired(Instant.now())) {
            throw new UnauthorizedException("refresh token expired or revoked");
        }

        refreshTokenRepository.revokeByTokenHash(tokenHash);
        String newToken = issueToken(existing.userId());

        return new RotationResult(existing.userId(), newToken);
    }

    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // ---- helpers ----

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("failed to hash refresh token", e);
        }
    }
}
