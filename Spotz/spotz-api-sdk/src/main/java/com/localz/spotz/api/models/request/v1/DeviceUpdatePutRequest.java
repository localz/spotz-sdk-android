package com.localz.spotz.api.models.request.v1;

import com.google.gson.annotations.SerializedName;

public class DeviceUpdatePutRequest {
    @SerializedName("appBuild")
    public String applicationBuild;
    @SerializedName("appVer")
    public String applicationVersion;
    public String deviceOs;
    @SerializedName("deviceOsVer")
    public String deviceOsVersion;
    public String locale;
    @SerializedName("lang")
    public String language;
    public Notification notification;
    @SerializedName("sdkVer")
    public String sdkVersion;
    public String timeZone;

    public static class Notification {
        public String pushId;
        public Boolean pushEnabled;
    }
}
