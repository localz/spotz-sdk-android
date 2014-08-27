package com.localz.spotz.api.models.request.v1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.localz.spotz.api.utils.DateUtils;

import java.util.Date;

public class ActivityReportPostRequest {
    @Expose
    public String dateTime;
    @Expose
    public String eventType;

    @Expose
    public String eventId;

    public String beaconId;
    public String spotzId;

    public ActivityReportPostRequest(Date dateTimeObject, String eventType, String eventId, String beaconId, String spotzId) {
        this.dateTime = DateUtils.dateToIso8601Date(dateTimeObject);
        this.eventType = eventType;
        this.eventId = eventId;
        this.beaconId = beaconId;
        this.spotzId = spotzId;
    }
}
