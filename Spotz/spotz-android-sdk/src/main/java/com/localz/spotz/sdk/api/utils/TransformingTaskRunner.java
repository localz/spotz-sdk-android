package com.localz.spotz.sdk.api.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

public class TransformingTaskRunner<T, V, R> extends TaskRunner<T, V> {

    private final ResponseListenerAdapter<R> listener;
    private final ModelTransformer<V, R> transformer;
    private final Context context;

    public TransformingTaskRunner(ApiMethod<T, V> apiMethod, ResponseListenerAdapter<R> listener,
                                  Context context, ModelTransformer<V, R> transformer) {
        super(apiMethod, listener);
        this.context = context;
        this.listener = listener;
        this.transformer = transformer;
    }

    @Override
    protected void onPostExecute(final Response<V> response) {
        if (response != null) {
            if (listener != null) {
                final Response<R> newResponse = new Response<R>();
                Response.copy(response, newResponse);

                if (response.isSuccess) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            newResponse.data = transformer.transform(context, response.data);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            listener.onSuccess(newResponse);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else {
                    listener.onError(newResponse, null);
                }
            }
        }
    }
}
