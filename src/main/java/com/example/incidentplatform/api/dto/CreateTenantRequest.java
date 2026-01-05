package com.example.incidentplatform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-z0-9-]{3,64}$")
        String slug,

        @NotBlank
        @Size(max = 255)
        String name
) {}
