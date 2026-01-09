package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.CreateTenantUserUseCase;
import com.example.incidentplatform.domain.model.RoleCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/users")
public class TenantUserController {

    private final CreateTenantUserUseCase createTenantUserUseCase;

    public TenantUserController(CreateTenantUserUseCase createTenantUserUseCase) {
        this.createTenantUserUseCase = createTenantUserUseCase;
    }

    @PostMapping
    public ResponseEntity<String> addUserToTenant(
            @PathVariable UUID tenantId,
            @RequestParam UUID userId,
            @RequestParam RoleCode roleCode) {

        createTenantUserUseCase.execute(tenantId, userId, roleCode);

        return ResponseEntity.ok("User added to tenant");
    }

    @DeleteMapping
    public ResponseEntity<String> removeUserFromTenant(
            @PathVariable UUID tenantId,
            @RequestParam UUID userId) {

        createTenantUserUseCase.removeUserFromTenant(tenantId, userId);

        return ResponseEntity.ok("User removed from tenant");
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isUserMember(
            @PathVariable UUID tenantId,
            @RequestParam UUID userId) {

        boolean isMember = createTenantUserUseCase.isUserMember(tenantId, userId);

        return ResponseEntity.ok(isMember);
    }
}
