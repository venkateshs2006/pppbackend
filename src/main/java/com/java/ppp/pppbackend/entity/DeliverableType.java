package com.java.ppp.pppbackend.entity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliverableType {
    // Keep your constants UPPERCASE (Standard Java Convention)
    GUIDE, TOPIC, POLICY, PROCEDURE, TEMPLATE;

    // This allows Jackson to accept "Policy", "policy", or "POLICY"
    @JsonCreator
    public static DeliverableType fromString(String value) {
        if (value == null) return null;
        try {
            return DeliverableType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle unknown values gracefully or throw better error
            throw new IllegalArgumentException("Unknown DeliverableType: " + value);
        }
    }

    // Optional: This controls how it looks when sent BACK to the frontend
    @JsonValue
    public String toValue() {
        return this.name(); // Returns "POLICY"
        // OR return this.name().toLowerCase(); // Returns "policy"
    }
}