package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.UserRepository;
import com.example.incidentplatform.domain.model.user.User;
import com.example.incidentplatform.infrastructure.persistence.entity.UserEntity;
import com.example.incidentplatform.infrastructure.persistence.mapper.UserMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.UserJpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public JpaUserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = userJpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }
}
