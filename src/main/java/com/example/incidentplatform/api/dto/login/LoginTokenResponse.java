package com.example.incidentplatform.api.dto.login;

public record LoginTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}
