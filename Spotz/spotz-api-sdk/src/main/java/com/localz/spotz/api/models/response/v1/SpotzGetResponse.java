package com.localz.spotz.api.models.response.v1;

import com.google.gson.reflect.TypeToken;
import com.localz.spotz.api.models.Response;

import java.lang.reflect.Type;
import java.util.Map;

public class SpotzGetResponse {
    public static final Type TYPE = new TypeToken<Response<SpotzGetResponse[]>>() {
    }.getType();


    public String _id;
    public String name;
    public String tag;
    public Beacon[] beacons;
    public String uuid;
    public String major;
    public String minor;
    public Location loc;
    public int radius;
    public Metadata[] metadata;
    public String account;


    public static class Beacon {
        public int[] xy;
        public int radius;
        public String serial;
        public String vendor;
        public String vendorId;
    }

    public static class Location {
        public String type;
        public int[] coordinates;
    }

    public static class Metadata {
        public String key;
        public String val;
    }
}