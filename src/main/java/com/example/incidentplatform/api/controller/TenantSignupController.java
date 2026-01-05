package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.CreateTenantRequest;
import com.example.incidentplatform.api.dto.TenantResponse;
import com.example.incidentplatform.application.usecase.CreateTenantUseCase;
import com.example.incidentplatform.domain.model.Tenant;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/tenants")
public class TenantSignupController {

    private final CreateTenantUseCase createTenantUseCase;

    public TenantSignupController(CreateTenantUseCase createTenantUseCase) {
        this.createTenantUseCase = createTenantUseCase;
    }

    @PostMapping
    public TenantResponse create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = createTenantUseCase.create(request.slug(), request.name());
        return new TenantResponse(tenant.id(), tenant.slug(), tenant.name(), tenant.status().name());
    }
}
