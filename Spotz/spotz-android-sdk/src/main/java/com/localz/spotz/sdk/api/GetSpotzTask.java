package com.localz.spotz.sdk.api;

import com.localz.spotz.api.models.request.v1.SpotzGetRequest;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.api.v1.SpotzGetApi;
import com.localz.spotz.sdk.api.utils.ApiTask;
import com.localz.spotz.sdk.api.utils.DefaultTaskRunner;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class GetSpotzTask extends ApiTask<SpotzGetRequest> {

    public GetSpotzTask(ResponseListenerAdapter<SpotzGetResponse> listener) {
        super(new DefaultTaskRunner<SpotzGetRequest, SpotzGetResponse>(
                new SpotzGetApi(), listener));
    }
}
