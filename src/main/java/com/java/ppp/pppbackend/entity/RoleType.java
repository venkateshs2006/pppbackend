package com.java.ppp.pppbackend.entity;

public enum RoleType {
    // Define enum constants in standard Java UPPER_CASE
    LEAD_CONSULTANT("lead_consultant"),
    SUB_CONSULTANT("sub_consultant"),
    MAIN_CLIENT("main_client"),
    SUB_CLIENT("sub_client"),
    SUPER_ADMIN("super_admin"),
    ADMIN("admin");

    private final String dbValue;

    RoleType(String dbValue) {
        this.dbValue = dbValue;
    }

    // Helper to convert DB string back to Enum
    public static RoleType fromDbValue(String dbValue) {
        for (RoleType type : RoleType.values()) {
            if (type.dbValue.equalsIgnoreCase(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + dbValue);
    }

    public String getDbValue() {
        return dbValue;
    }
}