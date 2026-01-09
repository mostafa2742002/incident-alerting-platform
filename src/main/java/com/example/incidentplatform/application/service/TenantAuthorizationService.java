package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.TenantUserRepository;
import com.example.incidentplatform.domain.model.RoleCode;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class TenantAuthorizationService {

    private final TenantUserRepository tenantUserRepository;

    public TenantAuthorizationService(TenantUserRepository tenantUserRepository) {
        this.tenantUserRepository = tenantUserRepository;
    }

    public boolean userHasRoleInTenant(UUID tenantId, UUID userId, RoleCode requiredRole) {
        return tenantUserRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(membership -> membership.roleCode().ordinal() <= requiredRole.ordinal())
                .orElse(false);
    }

    public boolean userCanManageMembersInTenant(UUID tenantId, UUID userId) {
        return tenantUserRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(membership -> membership.roleCode() == RoleCode.OWNER || membership.roleCode() == RoleCode.ADMIN)
                .orElse(false);
    }
}
