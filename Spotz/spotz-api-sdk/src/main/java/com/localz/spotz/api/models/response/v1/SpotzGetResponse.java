package com.localz.spotz.api.models.response.v1;

import com.google.gson.reflect.TypeToken;
import com.localz.spotz.api.models.Response;

import java.io.Serializable;
import java.lang.reflect.Type;

public class SpotzGetResponse implements Serializable {
    public static final Type TYPE = new TypeToken<Response<SpotzGetResponse>>() {
    }.getType();

    public static final Type TYPE_ARRAY = new TypeToken<Response<SpotzGetResponse[]>>() {
    }.getType();


    public String _id;
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
        public String val;
    }
}