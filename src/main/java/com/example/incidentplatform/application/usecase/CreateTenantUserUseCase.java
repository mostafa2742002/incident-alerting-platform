package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.service.TenantUserService;
import com.example.incidentplatform.domain.model.RoleCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateTenantUserUseCase {

    private final TenantUserService tenantUserService;

    public CreateTenantUserUseCase(TenantUserService tenantUserService) {
        this.tenantUserService = tenantUserService;
    }


    public void execute(UUID tenantId, UUID userId, RoleCode roleCode) {
        tenantUserService.addUserToTenant(tenantId, userId, roleCode);
    }

    public void removeUserFromTenant(UUID tenantId, UUID userId) {
        tenantUserService.removeUserFromTenant(tenantId, userId);
    }

    public boolean isUserMember(UUID tenantId, UUID userId) {
        return tenantUserService.isUserMember(tenantId, userId);
    }
}
