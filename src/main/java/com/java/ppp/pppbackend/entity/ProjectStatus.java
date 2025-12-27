package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    PLANNING("planning"),
    ACTIVE("active"),
    ON_HOLD("on_hold"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String dbValue;

    ProjectStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public static ProjectStatus fromDbValue(String dbValue) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + dbValue);
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }
}