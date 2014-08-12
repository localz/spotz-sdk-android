package com.localz.spotz.sdk.api.utils;

import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class DefaultTaskRunner<T, V> extends TaskRunner<T, V> {

    private final ResponseListenerAdapter<V> listener;

    public DefaultTaskRunner(ApiMethod<T, V> apiMethod, ResponseListenerAdapter<V> listener) {
        super(apiMethod, listener);
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Response<V> response) {
        if (response != null) {
            if (listener != null) {
                if (response.isSuccess) {
                    listener.onSuccess(response);
                } else {
                    listener.onError(response, null);
                }
            }
        }
    }
}
