package com.localz.spotz.sdk.api;

import com.localz.spotz.api.models.request.v1.DeviceRegisterPostRequest;
import com.localz.spotz.api.models.response.v1.DeviceRegisterPostResponse;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.api.v1.DeviceRegisterPostApi;
import com.localz.spotz.api.v1.SpotzGetApi;
import com.localz.spotz.sdk.api.utils.ApiTask;
import com.localz.spotz.sdk.api.utils.DefaultTaskRunner;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class SpotzListingsTask extends ApiTask<Void> {

    public SpotzListingsTask(ResponseListenerAdapter<SpotzGetResponse[]> listener) {
        super(new DefaultTaskRunner<Void, SpotzGetResponse[]>(
                new SpotzGetApi(), listener));
    }
}
