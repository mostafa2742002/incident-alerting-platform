package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.membership.AddMembershipRequest;
import com.example.incidentplatform.application.usecase.CreateTenantUserUseCase;
import com.example.incidentplatform.domain.model.user.RoleCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TenantUserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateTenantUserUseCase createTenantUserUseCase;

    @InjectMocks
    private TenantUserController tenantUserController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantUserController).build();
    }

    @Test
    @DisplayName("POST /api/public/tenants/{tenantId}/users adds user to tenant")
    void addUserToTenant_returnsOk() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var request = new AddMembershipRequest(userId, RoleCode.MEMBER);

        mockMvc.perform(post("/api/public/tenants/{tenantId}/users", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(userId.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(RoleCode.MEMBER.name())));

        verify(createTenantUserUseCase).execute(tenantId, userId, RoleCode.MEMBER);
    }

    @Test
    @DisplayName("DELETE /api/public/tenants/{tenantId}/users removes user from tenant")
    void removeUserFromTenant_returnsOk() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/public/tenants/{tenantId}/users", tenantId)
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("removed")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("true")));

        verify(createTenantUserUseCase).removeUserFromTenant(tenantId, userId);
    }

    @Test
    @DisplayName("GET /api/public/tenants/{tenantId}/users/check indicates membership")
    void isUserMember_returnsBoolean() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(createTenantUserUseCase.isUserMember(tenantId, userId)).thenReturn(true);

        mockMvc.perform(get("/api/public/tenants/{tenantId}/users/check", tenantId)
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("member")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("true")));

        verify(createTenantUserUseCase).isUserMember(tenantId, userId);
    }

    @Test
    @DisplayName("GET /api/public/tenants/{tenantId}/users lists members")
    void listMembers_returnsOk_with_json() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var member = com.example.incidentplatform.domain.model.tenant.TenantUser.createNew(tenantId, userId, RoleCode.MEMBER);
        when(createTenantUserUseCase.listMembers(tenantId)).thenReturn(java.util.List.of(member));

        mockMvc.perform(get("/api/public/tenants/{tenantId}/users", tenantId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(userId.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(RoleCode.MEMBER.name())));

        verify(createTenantUserUseCase).listMembers(tenantId);
    }

    @Test
    @DisplayName("GET /api/public/tenants/{tenantId}/users/{userId} returns membership")
    void getMembership_returnsOk() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var member = com.example.incidentplatform.domain.model.tenant.TenantUser.createNew(tenantId, userId, RoleCode.ADMIN);
        when(createTenantUserUseCase.getMembership(tenantId, userId)).thenReturn(member);

        mockMvc.perform(get("/api/public/tenants/{tenantId}/users/{userId}", tenantId, userId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(userId.toString())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(RoleCode.ADMIN.name())));

        verify(createTenantUserUseCase).getMembership(tenantId, userId);
    }
}
