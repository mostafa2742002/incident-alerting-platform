package com.example.incidentplatform.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record AssignUserRequest(
        @NotNull(message = "Assignee ID is required") UUID assigneeId,

        String notes) {
}
