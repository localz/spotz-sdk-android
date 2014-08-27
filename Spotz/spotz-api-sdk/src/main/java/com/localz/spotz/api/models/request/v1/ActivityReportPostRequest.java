package com.localz.spotz.api.models.request.v1;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ActivityReportPostRequest {
    @SerializedName("dateTime")
    public Date dateTime;
    public String eventType;
}
