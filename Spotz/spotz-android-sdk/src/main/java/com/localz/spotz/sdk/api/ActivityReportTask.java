package com.localz.spotz.sdk.api;

import com.localz.spotz.api.models.request.v1.ActivityReportPostRequest;
import com.localz.spotz.api.models.response.v1.ActivityReportPostResponse;
import com.localz.spotz.api.v1.ActivityReportPostApi;
import com.localz.spotz.sdk.api.utils.ApiTask;
import com.localz.spotz.sdk.api.utils.DefaultTaskRunner;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class ActivityReportTask extends ApiTask<ActivityReportPostRequest> {

    public ActivityReportTask(ResponseListenerAdapter<ActivityReportPostResponse> listener) {
        super(new DefaultTaskRunner<ActivityReportPostRequest, ActivityReportPostResponse>(
                new ActivityReportPostApi(), listener));
    }
}
