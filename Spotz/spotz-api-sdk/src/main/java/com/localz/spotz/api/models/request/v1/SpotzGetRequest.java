package com.localz.spotz.api.models.request.v1;

public class SpotzGetRequest {
    public String spotzId;
    public String uuid;
    public Integer major;
    public Integer minor;

    public SpotzGetRequest(String spotzId) {
        this.spotzId = spotzId;
    }

    public SpotzGetRequest(String uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }
}
