package com.example.incidentplatform.api.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTenantRequest(

                @NotBlank @Size(min = 3, max = 64) @Pattern(regexp = "^[a-z0-9-]{3,64}$") String slug,

                @NotBlank @Size(max = 255) String name,

                UUID ownerId) {
}
