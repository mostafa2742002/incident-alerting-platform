package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.Tenant;

import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findBySlug(String slug);

    Tenant save(Tenant tenant);
}