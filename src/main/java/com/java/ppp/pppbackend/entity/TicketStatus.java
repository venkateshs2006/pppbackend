package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    WAITING_RESPONSE("waiting_response"),
    CLOSED("closed"),
    FOR_REVIEW("for_review"),
    REDO("redo"),
    RESOLVED("resolved"),
    PLANNING("planning"),
    PENDING_CLIENT_APPROVAL("pending_client_approval");
    private final String dbValue;

    TicketStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public static TicketStatus fromDbValue(String dbValue) {
        if (dbValue == null) return null;
        for (TicketStatus status : TicketStatus.values()) {
            if (status.dbValue.equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ticket status: " + dbValue);
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }
}