package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.LoginRequest;
import com.example.incidentplatform.api.dto.LoginResponse;
import com.example.incidentplatform.api.dto.RegisterUserRequest;
import com.example.incidentplatform.api.dto.UserResponse;
import com.example.incidentplatform.application.usecase.LoginUseCase;
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
    private final LoginUseCase loginUseCase;

    public AuthPublicController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(user.id(), user.email(), user.status().name()));
    }

}
