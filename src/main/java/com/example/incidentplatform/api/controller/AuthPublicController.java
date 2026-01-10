package com.example.incidentplatform.api.controller;

import org.springframework.security.core.Authentication;

import com.example.incidentplatform.api.dto.login.LoginRequest;
import com.example.incidentplatform.api.dto.login.LoginTokenResponse;
import com.example.incidentplatform.api.dto.login.RegisterUserRequest;
import com.example.incidentplatform.api.dto.refresh.RefreshRequest;
import com.example.incidentplatform.api.dto.refresh.RefreshResponse;
import com.example.incidentplatform.api.dto.user.UserResponse;
import com.example.incidentplatform.application.service.RefreshTokenService;
import com.example.incidentplatform.application.usecase.LoginUseCase;
import com.example.incidentplatform.application.usecase.RefreshAuthUseCase;
import com.example.incidentplatform.application.usecase.RegisterUserUseCase;
import com.example.incidentplatform.domain.model.user.User;
import com.example.incidentplatform.infrastructure.security.JwtTokenService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/auth")
public class AuthPublicController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final JwtTokenService jwtTokenService;
    private final RefreshAuthUseCase refreshAuthUseCase;
    private final RefreshTokenService refreshTokenService;



    public AuthPublicController(RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase,
            JwtTokenService jwtTokenService,
            RefreshAuthUseCase refreshAuthUseCase,
            RefreshTokenService refreshTokenService) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.jwtTokenService = jwtTokenService;
        this.refreshAuthUseCase = refreshAuthUseCase;
        this.refreshTokenService = refreshTokenService;
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
    public ResponseEntity<LoginTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = loginUseCase.execute(request.email(), request.password());

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issueToken(user.id());

        return ResponseEntity.ok(new LoginTokenResponse(accessToken, refreshToken, "Bearer"));
    }



    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var result = refreshAuthUseCase.execute(request.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(result.accessToken(), result.refreshToken(), result.tokenType()));
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        UUID userId = UUID.fromString(authentication.getName());

        refreshTokenService.revokeAll(userId);
        return ResponseEntity.noContent().build();
    }



}
