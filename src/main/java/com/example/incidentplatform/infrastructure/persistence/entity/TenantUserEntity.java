package com.example.incidentplatform.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity for tenant_users table (tenant membership).
 */
@Entity
@Table(name = "tenant_users")
public class TenantUserEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_code", nullable = false, length = 32)
    private String roleCode;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TenantUserEntity() {
    }

    public TenantUserEntity(
            UUID id,
            UUID tenantId,
            UUID userId,
            String roleCode,
            OffsetDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.roleCode = roleCode;
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
