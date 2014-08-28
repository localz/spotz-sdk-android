package com.localz.spotz.api.models.request.v1;

import com.google.gson.annotations.Expose;
import com.localz.spotz.api.utils.DateUtils;

import java.util.Date;

public class ActivityReportPostRequest {
    @Expose
    public String dateTime;
    @Expose
    public String eventType;

    public String beaconId;
    public String spotzId;

    public ActivityReportPostRequest(Date dateTimeObject, String eventType, String beaconId, String spotzId) {
        this.dateTime = DateUtils.dateToIso8601Date(dateTimeObject);
        this.eventType = eventType;
        this.beaconId = beaconId;
        this.spotzId = spotzId;
    }
}
