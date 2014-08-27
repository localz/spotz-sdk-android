package com.localz.spotz.sdk.models;

import com.localz.spotz.api.models.response.v1.SpotzGetResponse;

import java.io.Serializable;

public class Spot implements Serializable {
    public String id;
    public String name;
    public String tag;
    public Beacon[] beacons;
    public Location loc;
    public Integer radius;
    public Metadata[] metadata;
    public String appId;
    public Boolean deleted;

    public static class Beacon implements Serializable {
        public String beaconId;
        public String uuid;
        public Integer major;
        public Integer minor;
    }

    public static class Location implements Serializable {
        public String type;
        public int[] coordinates;
    }

    public static class Metadata implements Serializable {
        public String key;
        public String value;
    }

    public static Spot clone(SpotzGetResponse spotzGetResponse) {
        Spot spot = new Spot();
        spot.id = spotzGetResponse._id;
        spot.name = spotzGetResponse.name;
        spot.tag = spotzGetResponse.tag;
        spot.radius = spotzGetResponse.radius;
        spot.appId = spotzGetResponse.appId;
        spot.deleted = spotzGetResponse.deleted;
        if (spotzGetResponse.beacons != null) {
            spot.beacons = new Beacon[spotzGetResponse.beacons.length];
            for (int i = 0; i < spotzGetResponse.beacons.length; i++) {
                spot.beacons[i] = new Beacon();
                spot.beacons[i].beaconId = spotzGetResponse.beacons[i].beaconId;
                spot.beacons[i].uuid = spotzGetResponse.beacons[i].uuid;
                spot.beacons[i].major = spotzGetResponse.beacons[i].major;
                spot.beacons[i].minor = spotzGetResponse.beacons[i].minor;
            }
        }

        if (spotzGetResponse.loc != null) {
            spot.loc = new Location();
            spot.loc.type = spotzGetResponse.loc.type;
            spot.loc.coordinates = spotzGetResponse.loc.coordinates;
        }

        if (spotzGetResponse.metadata != null) {
            spot.metadata = new Metadata[spotzGetResponse.metadata.length];
            for (int i = 0; i < spotzGetResponse.metadata.length; i++) {
                spot.metadata[i] = new Metadata();
                spot.metadata[i].key = spotzGetResponse.metadata[i].key;
                spot.metadata[i].value = spotzGetResponse.metadata[i].val;
            }
        }

        return spot;
    }
}
