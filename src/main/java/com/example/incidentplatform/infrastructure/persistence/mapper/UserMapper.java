package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.User;
import com.example.incidentplatform.domain.model.UserStatus;
import com.example.incidentplatform.infrastructure.persistence.entity.UserEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class UserMapper {
    private UserMapper() {}

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getPasswordHash(),
                UserStatus.valueOf(entity.getStatus())
        );
    }

    public static UserEntity toEntity(User user) {
        UUID id = user.id();
        OffsetDateTime now = OffsetDateTime.now();

        return new UserEntity(
                id,
                user.email(),
                user.displayName(),
                user.passwordHash(),
                user.status().name(),
                now,
                now
        );
    }
}
