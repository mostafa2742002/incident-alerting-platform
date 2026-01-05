package com.example.incidentplatform.application.usecase;

import org.springframework.stereotype.Service;

import com.example.incidentplatform.application.port.TenantRepository;
import com.example.incidentplatform.domain.model.Tenant;

@Service
public class CreateTenantUseCase {

    private final TenantRepository tenantRepository;

    public CreateTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant create(String slug, String name) {
        tenantRepository.findBySlug(slug).ifPresent(existingTenant -> {
            throw new IllegalArgumentException("Tenant with slug " + slug + " already exists.");
        });

        Tenant newTenant = Tenant.createNew(slug, name);
        return tenantRepository.save(newTenant);
    }
}
