package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    CRITICAL("critical"),
    URGENT("critical"); // Handle mapping URGENT to CRITICAL if needed, or add URGENT("urgent")

    private final String dbValue;

    TicketPriority(String dbValue) {
        this.dbValue = dbValue;
    }
    // ✅ FIX: Allow Jackson to match "LOW", "low", "Low" etc.
    @JsonCreator
    public static TicketPriority fromString(String value) {
        if (value == null) return null;
        for (TicketPriority priority : TicketPriority.values()) {
            if (priority.name().equalsIgnoreCase(value) || priority.dbValue.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        // Fallback or throw error
        throw new IllegalArgumentException("Unknown ticket priority: " + value);
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }
}
