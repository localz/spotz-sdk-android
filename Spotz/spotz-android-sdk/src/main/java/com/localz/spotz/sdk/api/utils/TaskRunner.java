package com.localz.spotz.sdk.api.utils;

import android.os.AsyncTask;

import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.exceptions.LocalzApiException;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class TaskRunner<T, V> extends AsyncTask<T, Void, Response<V>> {

    private final ApiMethod<T, V> apiMethod;
    private final ResponseListenerAdapter listener;

    public TaskRunner(ApiMethod<T, V> apiMethod, ResponseListenerAdapter listener) {
        this.apiMethod = apiMethod;
        this.listener = listener;
    }

    @Override
    protected Response<V> doInBackground(T... params) {
        try {
            if (params == null || params.length == 0)
                return apiMethod.execute();
            return apiMethod.execute(params[0]);
        } catch (LocalzApiException e) {
            if (listener != null) {
                //noinspection unchecked
                listener.onError(null, e);
            }
        }

        return null;
    }
}
