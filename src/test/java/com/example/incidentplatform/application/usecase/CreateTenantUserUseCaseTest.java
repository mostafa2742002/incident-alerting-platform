package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.service.TenantAuthorizationService;
import com.example.incidentplatform.application.service.TenantUserService;
import com.example.incidentplatform.common.error.ForbiddenException;
import com.example.incidentplatform.domain.model.user.RoleCode;
import com.example.incidentplatform.infrastructure.security.SecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CreateTenantUserUseCaseTest {

    @Mock
    private TenantUserService tenantUserService;

    @Mock
    private TenantAuthorizationService tenantAuthorizationService;

    @Mock
    private SecurityContextHelper securityContextHelper;

    private CreateTenantUserUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new CreateTenantUserUseCase(
                tenantUserService,
                tenantAuthorizationService,
                securityContextHelper);
    }

    @Test
    @DisplayName("execute adds user when current user is OWNER in tenant")
    void execute_addsUser_whenUserIsOwnerInTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        RoleCode roleCode = RoleCode.MEMBER;

        when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.of(currentUserId));
        when(tenantAuthorizationService.userCanManageMembersInTenant(tenantId, currentUserId))
                .thenReturn(true);

        useCase.execute(tenantId, newUserId, roleCode);

        verify(tenantUserService).addUserToTenant(tenantId, newUserId, roleCode);
    }

    @Test
    @DisplayName("execute throws ForbiddenException when user is not OWNER/ADMIN in tenant")
    void execute_throwsForbidden_whenUserCannotManageMembers() {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        RoleCode roleCode = RoleCode.MEMBER;

        when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.of(currentUserId));
        when(tenantAuthorizationService.userCanManageMembersInTenant(tenantId, currentUserId))
                .thenReturn(false);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> useCase.execute(tenantId, newUserId, roleCode));

        assertEquals("User does not have permission to manage members in tenant: " + tenantId, ex.getMessage());
        verify(tenantUserService, never()).addUserToTenant(any(), any(), any());
    }

    @Test
    @DisplayName("execute throws ForbiddenException when user not authenticated")
    void execute_throwsForbidden_whenUserNotAuthenticated() {
        UUID tenantId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        RoleCode roleCode = RoleCode.MEMBER;

        when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.empty());

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> useCase.execute(tenantId, newUserId, roleCode));

        assertEquals("User not authenticated", ex.getMessage());
        verify(tenantUserService, never()).addUserToTenant(any(), any(), any());
    }

    @Test
    @DisplayName("removeUserFromTenant removes user when current user is ADMIN in tenant")
    void removeUserFromTenant_removesUser_whenUserIsAdminInTenant() {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID userToRemove = UUID.randomUUID();

        when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.of(currentUserId));
        when(tenantAuthorizationService.userCanManageMembersInTenant(tenantId, currentUserId))
                .thenReturn(true);

        useCase.removeUserFromTenant(tenantId, userToRemove);

        verify(tenantUserService).removeUserFromTenant(tenantId, userToRemove);
    }

    @Test
    @DisplayName("removeUserFromTenant throws ForbiddenException when user lacks permission")
    void removeUserFromTenant_throwsForbidden_whenUserCannotManageMembers() {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID userToRemove = UUID.randomUUID();

        when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.of(currentUserId));
        when(tenantAuthorizationService.userCanManageMembersInTenant(tenantId, currentUserId))
                .thenReturn(false);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> useCase.removeUserFromTenant(tenantId, userToRemove));

        assertEquals("User does not have permission to manage members in tenant: " + tenantId, ex.getMessage());
        verify(tenantUserService, never()).removeUserFromTenant(any(), any());
    }
}
