package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.CreateTenantRequest;
import com.example.incidentplatform.api.dto.TenantResponse;
import com.example.incidentplatform.application.usecase.CreateTenantUseCase;
import com.example.incidentplatform.application.usecase.GetTenantUseCase;
import com.example.incidentplatform.application.usecase.ListTenantsUseCase;
import com.example.incidentplatform.domain.model.tenant.Tenant;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/public/tenants")
public class TenantSignupController {

    private final CreateTenantUseCase createTenantUseCase;
    private final GetTenantUseCase getTenantUseCase;
    private final ListTenantsUseCase listTenantsUseCase;

    public TenantSignupController(CreateTenantUseCase createTenantUseCase, GetTenantUseCase getTenantUseCase, ListTenantsUseCase listTenantsUseCase) {
        this.createTenantUseCase = createTenantUseCase;
        this.getTenantUseCase = getTenantUseCase;
        this.listTenantsUseCase = listTenantsUseCase;
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


    @GetMapping
    public Page<TenantResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return listTenantsUseCase.list(pageable)
            .map(t -> new TenantResponse(t.id(), t.slug(), t.name(), t.status().name()));
    }


}
