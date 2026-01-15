package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    WAITING_RESPONSE("waiting_response"),
    RESOLVED("resolved"),
    CLOSED("closed"),
    FOR_REVIEW("for_review");

    private final String dbValue;

    TicketStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonCreator
    public static TicketStatus fromString(String value) {
        if (value == null) return null;
        for (TicketStatus status : TicketStatus.values()) {
            if (status.name().equalsIgnoreCase(value) || status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ticket status: " + value);
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }
}