package com.example.incidentplatform.common.error;

import java.time.Instant;
import java.util.List;

public record ApiValidationError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations
) {
    public record FieldViolation(String field, String message) {}
}
