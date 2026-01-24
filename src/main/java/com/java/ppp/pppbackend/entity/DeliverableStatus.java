package com.java.ppp.pppbackend.entity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliverableStatus {
    DRAFT, IN_PROGRESS, REDO, REVIEW, APPROVED, REJECTED, COMPLETED;

    @JsonCreator
    public static DeliverableStatus fromString(String value) {
        if (value == null) return null;
        try {
            return DeliverableStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown DeliverableStatus: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}