package com.example.incidentplatform.application.port;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.incidentplatform.domain.model.tenant.Tenant;

public interface TenantRepository {

    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findById(UUID id);
    Page<Tenant> findAll(Pageable pageable);


    Tenant save(Tenant tenant);
}