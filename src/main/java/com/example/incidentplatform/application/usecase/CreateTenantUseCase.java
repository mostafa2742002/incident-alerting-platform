package com.example.incidentplatform.application.usecase;

import org.springframework.stereotype.Service;

import com.example.incidentplatform.application.port.TenantRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.domain.model.tenant.Tenant;

@Service
public class CreateTenantUseCase {

    private final TenantRepository tenantRepository;

    public CreateTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant create(String slug, String name) {
        tenantRepository.findBySlug(slug).ifPresent(existingTenant -> {
            throw new ConflictException(
                "tenant slug already exists: " + slug );
        });

        Tenant newTenant = Tenant.createNew(slug, name);
        return tenantRepository.save(newTenant);
    }
}
