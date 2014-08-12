package com.localz.spotz.sdk.listeners;

import com.localz.spotz.api.models.Response;

public interface ResponseListener<T> {
    void onSuccess(Response<T> response);

    void onError(Response<T> response, Exception exception);
}
