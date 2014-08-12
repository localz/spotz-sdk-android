package com.localz.spotz.sdk.api;

import com.localz.spotz.api.models.request.v1.DeviceUpdatePutRequest;
import com.localz.spotz.api.models.response.v1.DeviceUpdatePutResponse;
import com.localz.spotz.api.v1.DeviceUpdatePutApi;
import com.localz.spotz.sdk.api.utils.ApiTask;
import com.localz.spotz.sdk.api.utils.DefaultTaskRunner;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class DeviceUpdateTask extends ApiTask<DeviceUpdatePutRequest> {

    public DeviceUpdateTask(ResponseListenerAdapter<DeviceUpdatePutResponse> listener) {
        super(new DefaultTaskRunner<DeviceUpdatePutRequest, DeviceUpdatePutResponse>(
                new DeviceUpdatePutApi(), listener));
    }
}
