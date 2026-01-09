package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.CreateTenantUserUseCase;
import com.example.incidentplatform.domain.model.RoleCode;
import com.example.incidentplatform.domain.model.TenantUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class UserMembershipControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateTenantUserUseCase createTenantUserUseCase;

    @InjectMocks
    private UserMembershipController userMembershipController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userMembershipController).build();
    }

    @Test
    @DisplayName("GET /api/public/users/{userId}/memberships returns all user memberships")
    void listUserMemberships_returnsListOfMemberships() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId1 = UUID.randomUUID();
        UUID tenantId2 = UUID.randomUUID();

       
        var membership1 = TenantUser.createNew(tenantId1, userId, RoleCode.OWNER);
        var membership2 = TenantUser.createNew(tenantId2, userId, RoleCode.MEMBER);
        var memberships = List.of(membership1, membership2);

        when(createTenantUserUseCase.listUserMemberships(userId)).thenReturn(memberships);

        mockMvc.perform(get("/api/public/users/{userId}/memberships", userId))
                .andExpect(status().isOk())
                // Verify both tenant IDs are in response
                .andExpect(content().string(org.hamcrest.Matchers.containsString(tenantId1.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(tenantId2.toString())))
                // Verify roles appear in response
                .andExpect(content().string(org.hamcrest.Matchers.containsString(RoleCode.OWNER.name())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(RoleCode.MEMBER.name())));

        verify(createTenantUserUseCase).listUserMemberships(userId);
    }

    @Test
    @DisplayName("GET /api/public/users/{userId}/memberships returns empty list for user with no memberships")
    void listUserMemberships_returnsEmptyList_whenUserHasNoMemberships() throws Exception {
        UUID userId = UUID.randomUUID();

      
        when(createTenantUserUseCase.listUserMemberships(userId)).thenReturn(List.of());

     
        mockMvc.perform(get("/api/public/users/{userId}/memberships", userId))
                .andExpect(status().isOk())
               
                .andExpect(content().string("[]"));

        verify(createTenantUserUseCase).listUserMemberships(userId);
    }

    @Test
    @DisplayName("GET /api/public/users/{userId}/memberships returns single membership")
    void listUserMemberships_returnsSingleMembership() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        var membership = TenantUser.createNew(tenantId, userId, RoleCode.ADMIN);
        when(createTenantUserUseCase.listUserMemberships(userId)).thenReturn(List.of(membership));

        mockMvc.perform(get("/api/public/users/{userId}/memberships", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(userId.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(tenantId.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(RoleCode.ADMIN.name())));

        verify(createTenantUserUseCase).listUserMemberships(userId);
    }
}
