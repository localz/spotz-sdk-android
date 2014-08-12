package com.localz.spotz.sdk.listeners;


import com.localz.spotz.api.models.Response;

public class ResponseListenerAdapter<T> implements ResponseListener<T> {
    @Override
    public void onSuccess(Response<T> response) {
    }

    @Override
    public void onError(Response<T> response, Exception exception) {
    }
}
