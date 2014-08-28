package com.localz.spotz.api.models.request.v1;

import com.google.gson.annotations.Expose;
import com.localz.spotz.api.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityReportPostRequest {
    @Expose
    public List<RecordData> records;

    public void addRecord(Date dateTimeObject, String eventType, String beaconId, String spotzId) {
        if (records == null) {
            records = new ArrayList<RecordData>();
        }
        records.add(new RecordData(dateTimeObject, eventType, beaconId, spotzId));
    }

    public int getLength() {
        int size = 0;
        if (records != null) {
            size = records.size();
        }
        return size;
    }


    public class RecordData {
        @Expose
        public String dateTime;
        @Expose
        public String eventType;

        @Expose
        public String beaconId;
        @Expose
        public String spotzId;

        public RecordData(Date dateTimeObject, String eventType, String beaconId, String spotzId) {
            this.dateTime = DateUtils.dateToIso8601Date(dateTimeObject);
            this.eventType = eventType;
            this.beaconId = beaconId;
            this.spotzId = spotzId;
        }
    }
}
