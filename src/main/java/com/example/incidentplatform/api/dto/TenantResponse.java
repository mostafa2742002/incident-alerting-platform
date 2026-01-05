package com.example.incidentplatform.api.dto;

import java.util.UUID;

public record TenantResponse(
        UUID id,
        String slug,
        String name,
        String status
) {}
