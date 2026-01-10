package com.example.incidentplatform.api.dto.membership;

import java.time.Instant;
import java.util.UUID;

import com.example.incidentplatform.api.dto.tenant.TenantUserResponse;
import com.example.incidentplatform.domain.model.user.RoleCode;

public record AddMembershipResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        RoleCode roleCode,
        Instant createdAt) {
    public static AddMembershipResponse from(TenantUserResponse res) {
        return new AddMembershipResponse(res.id(), res.tenantId(), res.userId(), res.roleCode(), res.createdAt());
    }
}
