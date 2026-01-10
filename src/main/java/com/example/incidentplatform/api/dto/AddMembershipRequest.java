package com.example.incidentplatform.api.dto;

import java.util.UUID;

import com.example.incidentplatform.domain.model.user.RoleCode;

public record AddMembershipRequest(
        UUID userId,
        RoleCode roleCode) {
}
