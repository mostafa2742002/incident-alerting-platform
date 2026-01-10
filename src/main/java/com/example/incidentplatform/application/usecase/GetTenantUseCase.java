package com.example.incidentplatform.application.usecase;

import java.util.UUID;


import org.springframework.stereotype.Service;

import com.example.incidentplatform.application.port.TenantRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.tenant.Tenant;

@Service
public class GetTenantUseCase {


    private final TenantRepository tenantRepository;

    public GetTenantUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant get(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("tenant not found: " + id));
    }
}
