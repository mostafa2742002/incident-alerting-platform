package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.TenantUserRepository;
import com.example.incidentplatform.domain.model.RoleCode;
import com.example.incidentplatform.domain.model.TenantUser;
import com.example.incidentplatform.infrastructure.persistence.entity.TenantEntity;
import com.example.incidentplatform.infrastructure.persistence.entity.UserEntity;
import com.example.incidentplatform.infrastructure.persistence.repository.TenantJpaRepository;
import com.example.incidentplatform.infrastructure.persistence.repository.TenantUserJpaRepository;
import com.example.incidentplatform.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaTenantUserRepositoryAdapter.class)
class JpaTenantUserRepositoryAdapterTest {

    @Autowired
    TenantUserJpaRepository tenantUserJpaRepository;

    @Autowired
    TenantJpaRepository tenantJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    TenantUserRepository adapter;

    @Test
    void adapter_round_trip_save_exists_delete() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        tenantJpaRepository.save(new TenantEntity(tenantId, "globex", "Globex", "ACTIVE",
                java.time.Instant.now(), java.time.Instant.now()));

        userJpaRepository.save(new UserEntity(userId, "adapter@test.com", "Adapter User", "hash", "ACTIVE",
                OffsetDateTime.now(), OffsetDateTime.now()));

        TenantUser member = TenantUser.createNew(tenantId, userId, RoleCode.MEMBER);
        TenantUser saved = adapter.save(member);

        assertThat(saved.roleCode()).isEqualTo(RoleCode.MEMBER);
        assertThat(adapter.existsByTenantIdAndUserId(tenantId, userId)).isTrue();

        adapter.deleteByTenantIdAndUserId(tenantId, userId);
        assertThat(adapter.existsByTenantIdAndUserId(tenantId, userId)).isFalse();
    }
}
