package com.example.incidentplatform.api.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String status
) {}
