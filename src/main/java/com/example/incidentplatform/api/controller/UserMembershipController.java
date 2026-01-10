package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.tenant.TenantUserResponse;
import com.example.incidentplatform.application.usecase.CreateTenantUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/users/{userId}/memberships")
public class UserMembershipController {

    private final CreateTenantUserUseCase useCase;

    public UserMembershipController(CreateTenantUserUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<TenantUserResponse>> listUserMemberships(@PathVariable UUID userId) {
        var memberships = useCase.listUserMemberships(userId).stream()
                .map(m -> new TenantUserResponse(m.id(), m.tenantId(), m.userId(), m.roleCode(), m.createdAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(memberships);
    }
}
