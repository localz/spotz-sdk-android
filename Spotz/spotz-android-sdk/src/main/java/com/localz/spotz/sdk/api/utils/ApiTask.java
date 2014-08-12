package com.localz.spotz.sdk.api.utils;

import android.os.AsyncTask;

public abstract class ApiTask<T> {

    private final TaskRunner runner;

    public ApiTask(TaskRunner runner) {
        this.runner = runner;
    }

    public void execute() {
        //noinspection unchecked
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void execute(T request) {
        //noinspection unchecked
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
    }
}
