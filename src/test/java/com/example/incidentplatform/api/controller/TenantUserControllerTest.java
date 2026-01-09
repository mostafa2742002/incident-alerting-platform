package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.CreateTenantUserUseCase;
import com.example.incidentplatform.domain.model.RoleCode;
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

        mockMvc.perform(post("/api/public/tenants/{tenantId}/users", tenantId)
                .param("userId", userId.toString())
                .param("roleCode", RoleCode.MEMBER.name())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("User added to tenant"));

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
                .andExpect(content().string("User removed from tenant"));

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
                .andExpect(content().string("true"));

        verify(createTenantUserUseCase).isUserMember(tenantId, userId);
    }
}
