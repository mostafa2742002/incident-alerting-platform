package com.example.incidentplatform.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.incidentplatform.application.port.PasswordHasher;
import com.example.incidentplatform.application.port.UserRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.domain.model.user.User;

@Component
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User execute(String email, String displayName, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("user email already exists: " + email);
        }

        String passwordHash = passwordHasher.hash(rawPassword);
        User user = User.createNew(UUID.randomUUID(), email, displayName, passwordHash);

        return userRepository.save(user);
    }
    
}
