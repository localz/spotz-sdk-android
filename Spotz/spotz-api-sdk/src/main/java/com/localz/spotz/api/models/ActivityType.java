package com.localz.spotz.api.models;

/**
 * Enum type for storing type of activity log entry.
 */
public enum ActivityType {

    SPOTZ_ENTER("se"),
    SPOTZ_EXIT("sx"),
    BEACON_ENTER("be"),
    BEACON_EXIT("bx");

    private final String name;

    private ActivityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
