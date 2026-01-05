package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.Tenant;
import com.example.incidentplatform.domain.model.TenantStatus;
import com.example.incidentplatform.infrastructure.persistence.entity.TenantEntity;

public final class TenantMapper {

    private TenantMapper() {
    }

    public static Tenant toDomain(TenantEntity entity) {
        return new Tenant(
                entity.getId(),
                entity.getSlug(),
                entity.getName(),
                TenantStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static TenantEntity toEntity(Tenant tenant) {
        return new TenantEntity(
                tenant.id(),
                tenant.slug(),
                tenant.name(),
                tenant.status().name(),
                tenant.createdAt(),
                tenant.updatedAt()
        );
    }
}
