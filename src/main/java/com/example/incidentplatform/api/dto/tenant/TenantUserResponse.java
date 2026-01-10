package com.example.incidentplatform.api.dto.tenant;

import java.time.Instant;
import java.util.UUID;

import com.example.incidentplatform.domain.model.user.RoleCode;

public record TenantUserResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        RoleCode roleCode,
        Instant createdAt) {
}
