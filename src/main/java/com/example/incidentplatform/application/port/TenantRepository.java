package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {

    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findById(UUID id);

    Tenant save(Tenant tenant);
}