package com.example.incidentplatform.api.dto;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String email,
        String status
) {}
