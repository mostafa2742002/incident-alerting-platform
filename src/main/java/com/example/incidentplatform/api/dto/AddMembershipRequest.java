package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.RoleCode;
import java.util.UUID;

public record AddMembershipRequest(
        UUID userId,
        RoleCode roleCode) {
}
