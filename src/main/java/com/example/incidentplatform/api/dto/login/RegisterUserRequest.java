package com.example.incidentplatform.api.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 255) String displayName,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
