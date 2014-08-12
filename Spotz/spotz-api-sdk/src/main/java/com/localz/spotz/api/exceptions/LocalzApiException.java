package com.localz.spotz.api.exceptions;

public class LocalzApiException extends Exception {

    public LocalzApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalzApiException(Throwable cause) {
        super(cause);
    }
}
