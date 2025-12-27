package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    private final String dbValue;

    TicketPriority(String dbValue) {
        this.dbValue = dbValue;
    }

    public static TicketPriority fromDbValue(String dbValue) {
        if (dbValue == null) return null;
        for (TicketPriority priority : TicketPriority.values()) {
            if (priority.dbValue.equalsIgnoreCase(dbValue)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown ticket priority: " + dbValue);
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }
}
