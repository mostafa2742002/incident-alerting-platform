package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.tenant.TenantUser;
import com.example.incidentplatform.domain.model.user.RoleCode;
import com.example.incidentplatform.infrastructure.persistence.entity.TenantUserEntity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class TenantUserMapper {

    private TenantUserMapper() {
    }

    public static TenantUser toDomain(TenantUserEntity entity) {
        return new TenantUser(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                RoleCode.valueOf(entity.getRoleCode()),
                entity.getCreatedAt().toInstant());
    }

    public static TenantUserEntity toEntity(TenantUser tenantUser) {
        return new TenantUserEntity(
                tenantUser.id(),
                tenantUser.tenantId(),
                tenantUser.userId(),
                tenantUser.roleCode().name(),
                OffsetDateTime.ofInstant(tenantUser.createdAt(), ZoneOffset.UTC));
    }
}
