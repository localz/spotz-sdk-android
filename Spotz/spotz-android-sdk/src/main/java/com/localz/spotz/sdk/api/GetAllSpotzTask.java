package com.localz.spotz.sdk.api;

import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.api.v1.SpotzAllGetApi;
import com.localz.spotz.sdk.api.utils.ApiTask;
import com.localz.spotz.sdk.api.utils.DefaultTaskRunner;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class GetAllSpotzTask extends ApiTask<Void> {

    public GetAllSpotzTask(ResponseListenerAdapter<SpotzGetResponse[]> listener) {
        super(new DefaultTaskRunner<Void, SpotzGetResponse[]>(
                new SpotzAllGetApi(), listener));
    }
}
