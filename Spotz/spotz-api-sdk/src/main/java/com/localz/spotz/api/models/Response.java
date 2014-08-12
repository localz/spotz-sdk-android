package com.localz.spotz.api.models;

public class Response<T> {
    public boolean isSuccess;
    public int httpStatusCode;
    public String httpStatusMessage;
    public String code;
    public String message;
    public T data;

    public static <R> void copy(Response from, Response<R> to) {
        to.isSuccess = from.isSuccess;
        to.httpStatusCode = from.httpStatusCode;
        to.httpStatusMessage = from.httpStatusMessage;
        to.code = from.code;
        to.message = from.message;
    }
}
