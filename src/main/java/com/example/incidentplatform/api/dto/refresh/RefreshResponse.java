package com.example.incidentplatform.api.dto.refresh;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}
