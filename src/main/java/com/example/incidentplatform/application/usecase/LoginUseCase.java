package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.port.PasswordHasher;
import com.example.incidentplatform.application.port.UserRepository;
import com.example.incidentplatform.common.error.UnauthorizedException;
import com.example.incidentplatform.domain.model.user.User;

import org.springframework.stereotype.Component;

@Component
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public LoginUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User execute(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("invalid email or password"));

        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new UnauthorizedException("invalid email or password");
        }

        return user;
    }
}
