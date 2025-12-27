package com.java.ppp.pppbackend.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliverableType {
    GUIDE("guide"),
    TOPIC("topic"),
    POLICY("policy"),
    PROCEDURE("procedure"),
    TEMPLATE("template"),
    REPORT("report");

    private final String dbValue;

    DeliverableType(String dbValue) {
        this.dbValue = dbValue;
    }

    public static DeliverableType fromDbValue(String dbValue) {
        for (DeliverableType type : DeliverableType.values()) {
            if (type.dbValue.equalsIgnoreCase(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown deliverable type: " + dbValue);
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }
}