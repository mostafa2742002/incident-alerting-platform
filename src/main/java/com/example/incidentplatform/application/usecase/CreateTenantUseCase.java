package com.example.incidentplatform.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.incidentplatform.application.port.TenantRepository;
import com.example.incidentplatform.application.service.TenantUserService;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.domain.model.tenant.Tenant;
import com.example.incidentplatform.domain.model.user.RoleCode;

import java.util.UUID;

@Service
public class CreateTenantUseCase {

    private final TenantRepository tenantRepository;
    private final TenantUserService tenantUserService;

    public CreateTenantUseCase(TenantRepository tenantRepository, TenantUserService tenantUserService) {
        this.tenantRepository = tenantRepository;
        this.tenantUserService = tenantUserService;
    }

    @Transactional
    public Tenant create(String slug, String name, UUID ownerId) {
        tenantRepository.findBySlug(slug).ifPresent(existingTenant -> {
            throw new ConflictException(
                    "tenant slug already exists: " + slug);
        });

        Tenant newTenant = Tenant.createNew(slug, name);
        Tenant savedTenant = tenantRepository.save(newTenant);

        // Add owner to tenant if provided
        if (ownerId != null) {
            tenantUserService.addUserToTenant(savedTenant.id(), ownerId, RoleCode.OWNER);
        }

        return savedTenant;
    }
}
