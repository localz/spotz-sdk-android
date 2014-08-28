package com.localz.spotz.api.models;

/**
 * Enum type for storing type of activity log entry.
 */
public enum ActivityType {

    SPOTZ_ENTER("SpotzEnter"),
    SPOTZ_EXIT("SpotzExit"),
    BEACON_ENTER("BeaconEnter"),
    BEACON_EXIT("BeaconExit");

    private final String name;

    private ActivityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
