package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.CreateTenantUserUseCase;
import com.example.incidentplatform.domain.model.user.RoleCode;
import com.example.incidentplatform.api.dto.membership.AddMembershipRequest;
import com.example.incidentplatform.api.dto.membership.AddMembershipResponse;
import com.example.incidentplatform.api.dto.tenant.TenantUserResponse;
import com.example.incidentplatform.api.dto.user.MembershipCheckResponse;
import com.example.incidentplatform.api.dto.user.RemoveMembershipResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/users")
public class TenantUserController {

    private final CreateTenantUserUseCase createTenantUserUseCase;

    public TenantUserController(CreateTenantUserUseCase createTenantUserUseCase) {
        this.createTenantUserUseCase = createTenantUserUseCase;
    }

    @PostMapping
    public ResponseEntity<AddMembershipResponse> addUserToTenant(
            @PathVariable UUID tenantId,
            @RequestBody AddMembershipRequest request) {

        createTenantUserUseCase.execute(tenantId, request.userId(), request.roleCode());
        var res = new AddMembershipResponse(null, tenantId, request.userId(), request.roleCode(), java.time.Instant.now());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    public ResponseEntity<RemoveMembershipResponse> removeUserFromTenant(
            @PathVariable UUID tenantId,
            @RequestParam UUID userId) {

        createTenantUserUseCase.removeUserFromTenant(tenantId, userId);
        return ResponseEntity.ok(new RemoveMembershipResponse(true));
    }

    @GetMapping("/check")
    public ResponseEntity<MembershipCheckResponse> isUserMember(
            @PathVariable UUID tenantId,
            @RequestParam UUID userId) {

        boolean isMember = createTenantUserUseCase.isUserMember(tenantId, userId);
        return ResponseEntity.ok(new MembershipCheckResponse(isMember));
    }

    @GetMapping
    public ResponseEntity<List<TenantUserResponse>> listMembers(
            @PathVariable UUID tenantId) {
        var members = createTenantUserUseCase.listMembers(tenantId).stream()
                .map(m -> new TenantUserResponse(m.id(), m.tenantId(), m.userId(), m.roleCode(), m.createdAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<TenantUserResponse> getMembership(
            @PathVariable UUID tenantId,
            @PathVariable UUID userId) {
        var m = createTenantUserUseCase.getMembership(tenantId, userId);
        return ResponseEntity.ok(new TenantUserResponse(m.id(), m.tenantId(), m.userId(), m.roleCode(), m.createdAt()));
    }
}
