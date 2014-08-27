package com.localz.spotz.api.models;

/**
 * Enum type for storing type of activity log entry.
 */
public enum ActivityType {

    ENTRY("Entry"),
    INSPOTZ("InSpotz"), // beacon is visible first time in this cycle,
    // but we are already in the spotz that beacon belongs to
    // (due to other beacons in the spotz were visible in the previous scans)
    EXITED_BEACON_INSPOTZ("BeaconExitInSpotz"), // beacon was visible on previous scan, but not visible any more.
    // But user remains in the spotz (due to other beacons from the same spotz are visible).
    EXIT("Exit");

    private final String name;

    private ActivityType(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
