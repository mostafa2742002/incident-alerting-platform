package com.example.incidentplatform.api.dto;

public record LoginTokenResponse(
        String accessToken,
        String tokenType
) {}
