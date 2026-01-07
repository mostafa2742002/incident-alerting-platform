package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.port.UserRepository;
import com.example.incidentplatform.application.service.RefreshTokenService;
import com.example.incidentplatform.common.error.UnauthorizedException;
import com.example.incidentplatform.domain.model.User;
import com.example.incidentplatform.infrastructure.security.JwtTokenService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RefreshAuthUseCase {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    public RefreshAuthUseCase(
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            JwtTokenService jwtTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public Result execute(String refreshToken) {
        var rotated = refreshTokenService.rotate(refreshToken);

        UUID userId = rotated.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("user not found"));

        String accessToken = jwtTokenService.generateAccessToken(user);

        return new Result(accessToken, rotated.newRefreshToken(), "Bearer");
    }

    public record Result(String accessToken, String refreshToken, String tokenType) {}
}
