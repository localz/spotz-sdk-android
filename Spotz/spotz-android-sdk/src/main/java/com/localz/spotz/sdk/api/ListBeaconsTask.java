package com.localz.spotz.sdk.api;

import com.localz.spotz.api.models.response.v1.BeaconsGetResponse;
import com.localz.spotz.api.v1.BeaconsGetApi;
import com.localz.spotz.sdk.api.utils.ApiTask;
import com.localz.spotz.sdk.api.utils.DefaultTaskRunner;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class ListBeaconsTask extends ApiTask<Void> {

    public ListBeaconsTask(final ResponseListenerAdapter<BeaconsGetResponse[]> listener) {
        super(new DefaultTaskRunner<Void, BeaconsGetResponse[]>(
                new BeaconsGetApi(), listener));
    }
}
