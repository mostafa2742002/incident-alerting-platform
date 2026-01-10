package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.TenantUserRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.tenant.TenantUser;
import com.example.incidentplatform.domain.model.user.RoleCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantUserServiceTest {

    @Mock
    TenantUserRepository repository;

    @InjectMocks
    TenantUserService service;

    @Test
    void addUserToTenant_conflict_if_exists() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.existsByTenantIdAndUserId(tenantId, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.addUserToTenant(tenantId, userId, RoleCode.MEMBER))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void addUserToTenant_saves_when_not_exists() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.existsByTenantIdAndUserId(tenantId, userId)).thenReturn(false);

        TenantUser created = TenantUser.createNew(tenantId, userId, RoleCode.MEMBER);
        when(repository.save(any())).thenReturn(created);

        TenantUser saved = service.addUserToTenant(tenantId, userId, RoleCode.MEMBER);
        assertThat(saved).isNotNull();
        assertThat(saved.tenantId()).isEqualTo(tenantId);
        assertThat(saved.userId()).isEqualTo(userId);
        assertThat(saved.roleCode()).isEqualTo(RoleCode.MEMBER);
    }

    @Test
    void removeUserFromTenant_throws_not_found() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeUserFromTenant(tenantId, userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeUserFromTenant_deletes_when_found() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantUser member = TenantUser.createNew(tenantId, userId, RoleCode.MEMBER);
        when(repository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(member));

        service.removeUserFromTenant(tenantId, userId);
        verify(repository).delete(member);
    }

    @Test
    void isUserMember_delegates_to_repository() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.existsByTenantIdAndUserId(tenantId, userId)).thenReturn(true);

        assertThat(service.isUserMember(tenantId, userId)).isTrue();
    }

    @Test
    void listMembers_returns_members() {
        UUID tenantId = UUID.randomUUID();
        TenantUser member = TenantUser.createNew(tenantId, UUID.randomUUID(), RoleCode.ADMIN);
        when(repository.findByTenantId(tenantId)).thenReturn(List.of(member));

        List<TenantUser> res = service.listMembers(tenantId);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).roleCode()).isEqualTo(RoleCode.ADMIN);
    }
}
