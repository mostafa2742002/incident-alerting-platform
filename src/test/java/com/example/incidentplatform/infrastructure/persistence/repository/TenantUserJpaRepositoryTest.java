package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.TenantEntity;
import com.example.incidentplatform.infrastructure.persistence.entity.TenantUserEntity;
import com.example.incidentplatform.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TenantUserJpaRepositoryTest {

    @Autowired
    TenantUserJpaRepository tenantUserJpaRepository;

    @Autowired
    TenantJpaRepository tenantJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Test
    void save_and_query_membership() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Create tenant and user to satisfy FKs
        tenantJpaRepository.save(new TenantEntity(tenantId, "acme", "Acme Corp", "ACTIVE",
                java.time.Instant.now(), java.time.Instant.now()));

        userJpaRepository.save(new UserEntity(userId, "repo@test.com", "Repo User", "hash", "ACTIVE",
                OffsetDateTime.now(), OffsetDateTime.now()));

        // Create membership
        var entity = new TenantUserEntity(UUID.randomUUID(), tenantId, userId, "MEMBER", OffsetDateTime.now());
        tenantUserJpaRepository.save(entity);

        Optional<TenantUserEntity> found = tenantUserJpaRepository.findByTenantIdAndUserId(tenantId, userId);
        assertThat(found).isPresent();
        assertThat(found.get().getRoleCode()).isEqualTo("MEMBER");
        assertThat(tenantUserJpaRepository.existsByTenantIdAndUserId(tenantId, userId)).isTrue();

        tenantUserJpaRepository.deleteByTenantIdAndUserId(tenantId, userId);
        assertThat(tenantUserJpaRepository.existsByTenantIdAndUserId(tenantId, userId)).isFalse();
    }
}
