package com.example.incidentplatform.domain.model;

public enum Severity {
    CRITICAL(1),
    HIGH(2),
    MEDIUM(3),
    LOW(4);

    private final int level;

    Severity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public boolean isHigherThan(Severity other) {
        return this.level < other.level;
    }

    public boolean isLowerThan(Severity other) {
        return this.level > other.level;
    }

    /**
     * Escalate to next higher severity level.
     * LOW → MEDIUM → HIGH → CRITICAL
     * Returns CRITICAL if already at maximum.
     */
    public Severity escalate() {
        return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH -> CRITICAL;
            case CRITICAL -> CRITICAL; // Already at max
        };
    }

    /**
     * De-escalate to next lower severity level.
     * CRITICAL → HIGH → MEDIUM → LOW
     * Returns LOW if already at minimum.
     */
    public Severity deescalate() {
        return switch (this) {
            case CRITICAL -> HIGH;
            case HIGH -> MEDIUM;
            case MEDIUM -> LOW;
            case LOW -> LOW; // Already at min
        };
    }
}
