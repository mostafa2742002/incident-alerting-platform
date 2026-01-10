package com.example.incidentplatform.application.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.incidentplatform.application.port.TenantRepository;
import com.example.incidentplatform.domain.model.tenant.Tenant;

@Service
public class ListTenantsUseCase {

    private final TenantRepository tenantRepository;

    public ListTenantsUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Page<Tenant> list(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }
}
