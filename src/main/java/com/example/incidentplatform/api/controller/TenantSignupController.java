package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.CreateTenantRequest;
import com.example.incidentplatform.api.dto.TenantResponse;
import com.example.incidentplatform.application.usecase.CreateTenantUseCase;
import com.example.incidentplatform.application.usecase.GetTenantUseCase;
import com.example.incidentplatform.domain.model.Tenant;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/public/tenants")
public class TenantSignupController {

    private final CreateTenantUseCase createTenantUseCase;
    private final GetTenantUseCase getTenantUseCase;

    public TenantSignupController(CreateTenantUseCase createTenantUseCase, GetTenantUseCase getTenantUseCase) {
        this.createTenantUseCase = createTenantUseCase;
        this.getTenantUseCase = getTenantUseCase;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = createTenantUseCase.create(request.slug(), request.name());
        TenantResponse body = new TenantResponse(tenant.id(), tenant.slug(), tenant.name(), tenant.status().name());

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tenant.id())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public TenantResponse getById(@PathVariable UUID id) {
        Tenant tenant = getTenantUseCase.get(id);
        return new TenantResponse(tenant.id(), tenant.slug(), tenant.name(), tenant.status().name());
    }



}
