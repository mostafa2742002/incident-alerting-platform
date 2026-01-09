package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.RoleCode;
import java.time.Instant;
import java.util.UUID;

public record TenantUserResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        RoleCode roleCode,
        Instant createdAt) {
}
