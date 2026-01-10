package com.example.incidentplatform.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.incidentplatform.domain.model.tenant.Tenant;
import com.example.incidentplatform.domain.model.tenant.TenantStatus;

public class TenantTest {

    @Test
    void createNew_withValidSlug_createsActiveTenant() {
        Tenant tenant = Tenant.createNew("valid-slug", "Valid Name");
        assertNotNull(tenant.id());
        assertEquals("valid-slug", tenant.slug());
        assertEquals("Valid Name", tenant.name());
        assertEquals(TenantStatus.ACTIVE, tenant.status());
        assertNotNull(tenant.createdAt());
        assertNotNull(tenant.updatedAt());
    }

    @Test
    void createNew_withInvalidSlug_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Tenant.createNew("ACME!", "Acme Company"));
    }

}
