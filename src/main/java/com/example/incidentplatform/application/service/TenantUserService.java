package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.TenantUserRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.TenantUser;
import com.example.incidentplatform.domain.model.RoleCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantUserService {

    private final TenantUserRepository tenantUserRepository;

    public TenantUserService(TenantUserRepository tenantUserRepository) {
        this.tenantUserRepository = tenantUserRepository;
    }

    public TenantUser addUserToTenant(UUID tenantId, UUID userId, RoleCode roleCode) {
        if (tenantUserRepository.existsByTenantIdAndUserId(tenantId, userId)) {
            throw new ConflictException("User is already a member of this tenant");
        }
        TenantUser tenantUser = TenantUser.createNew(tenantId, userId, roleCode);
        return tenantUserRepository.save(tenantUser);
    }

    public void removeUserFromTenant(UUID tenantId, UUID userId) {
        TenantUser tenantUser = tenantUserRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new NotFoundException("User not found in the tenant"));
        tenantUserRepository.delete(tenantUser);
    }

    public boolean isUserMember(UUID tenantId, UUID userId) {
        return tenantUserRepository.existsByTenantIdAndUserId(tenantId, userId);
    }

    public java.util.List<TenantUser> listMembers(UUID tenantId) {
        return tenantUserRepository.findByTenantId(tenantId);
    }

    public java.util.List<TenantUser> listUserMemberships(UUID userId) {
        return tenantUserRepository.findByUserId(userId);
    }
}
