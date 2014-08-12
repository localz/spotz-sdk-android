package com.localz.spotz.api.utils;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;

public enum HttpRequestHolder {

    INSTANCE;

    private final HttpRequestFactory httpRequestFactory;

    private HttpRequestHolder() {
        NetHttpTransport transport = new NetHttpTransport.Builder().build();
        this.httpRequestFactory = transport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setConnectTimeout(20000);
                request.setReadTimeout(20000);
                request.setNumberOfRetries(2);
                request.setThrowExceptionOnExecuteError(false);
            }
        });
    }

    public HttpRequestFactory getHttpRequestFactory() {
        return httpRequestFactory;
    }
}
