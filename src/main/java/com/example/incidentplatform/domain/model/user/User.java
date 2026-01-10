package com.example.incidentplatform.domain.model.user;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record User(
        UUID id,
        String email,
        String displayName,
        String passwordHash,
        UserStatus status
) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public User {
        Objects.requireNonNull(id, "id must not be null");
        requireValidEmail(email);
        requireNonBlank(displayName, "displayName");
        requireNonBlank(passwordHash, "passwordHash");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static User createNew(UUID id, String email, String displayName, String passwordHash) {
        return new User(id, normalizeEmail(email), displayName.trim(), passwordHash, UserStatus.ACTIVE);
    }

    private static void requireValidEmail(String email) {
        requireNonBlank(email, "email");
        String normalized = normalizeEmail(email);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("invalid email format");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
