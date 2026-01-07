package com.example.incidentplatform.api.dto;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}
