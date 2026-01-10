package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.service.TenantUserService;
import com.example.incidentplatform.application.service.TenantAuthorizationService;
import com.example.incidentplatform.common.error.ForbiddenException;
import com.example.incidentplatform.domain.model.tenant.TenantUser;
import com.example.incidentplatform.domain.model.user.RoleCode;
import com.example.incidentplatform.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateTenantUserUseCase {

    private final TenantUserService tenantUserService;
    private final TenantAuthorizationService tenantAuthorizationService;
    private final SecurityContextHelper securityContextHelper;

    public CreateTenantUserUseCase(
            TenantUserService tenantUserService,
            TenantAuthorizationService tenantAuthorizationService,
            SecurityContextHelper securityContextHelper) {
        this.tenantUserService = tenantUserService;
        this.tenantAuthorizationService = tenantAuthorizationService;
        this.securityContextHelper = securityContextHelper;
    }

    public void execute(UUID tenantId, UUID userId, RoleCode roleCode) {
        UUID currentUserId = securityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));

        if (!tenantAuthorizationService.userCanManageMembersInTenant(tenantId, currentUserId)) {
            throw new ForbiddenException(
                    "User does not have permission to manage members in tenant: " + tenantId);
        }

        tenantUserService.addUserToTenant(tenantId, userId, roleCode);
    }

    public void removeUserFromTenant(UUID tenantId, UUID userId) {
        UUID currentUserId = securityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));

        if (!tenantAuthorizationService.userCanManageMembersInTenant(tenantId, currentUserId)) {
            throw new ForbiddenException(
                    "User does not have permission to manage members in tenant: " + tenantId);
        }

        tenantUserService.removeUserFromTenant(tenantId, userId);
    }

    public boolean isUserMember(UUID tenantId, UUID userId) {
        return tenantUserService.isUserMember(tenantId, userId);
    }

    public java.util.List<TenantUser> listMembers(UUID tenantId) {
        return tenantUserService.listMembers(tenantId);
    }

    public java.util.List<TenantUser> listUserMemberships(UUID userId) {
        return tenantUserService.listUserMemberships(userId);
    }

    public TenantUser getMembership(UUID tenantId, UUID userId) {
        return tenantUserService.getMembership(tenantId, userId);
    }
}
