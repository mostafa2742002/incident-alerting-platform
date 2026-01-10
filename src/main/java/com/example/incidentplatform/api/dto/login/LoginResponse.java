package com.example.incidentplatform.api.dto.login;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String email,
        String status
) {}
