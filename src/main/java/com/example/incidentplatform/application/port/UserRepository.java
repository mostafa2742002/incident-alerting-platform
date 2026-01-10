package com.example.incidentplatform.application.port;

import java.util.Optional;
import java.util.UUID;

import com.example.incidentplatform.domain.model.user.User;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
