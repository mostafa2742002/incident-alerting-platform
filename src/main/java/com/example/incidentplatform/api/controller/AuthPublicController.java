package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.RegisterUserRequest;
import com.example.incidentplatform.api.dto.UserResponse;
import com.example.incidentplatform.application.usecase.RegisterUserUseCase;
import com.example.incidentplatform.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/public/auth")
public class AuthPublicController {

    private final RegisterUserUseCase registerUserUseCase;

    public AuthPublicController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {
        User created = registerUserUseCase.execute(
                request.email(),
                request.displayName(),
                request.password()
        );

        return ResponseEntity
        .created(URI.create("/api/public/users/" + created.id()))
        .body(new UserResponse(
                created.id(),
                created.email(),
                created.displayName(),
                created.status().name()
        ));

    }
}
