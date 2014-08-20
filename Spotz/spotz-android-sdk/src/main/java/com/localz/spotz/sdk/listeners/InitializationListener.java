package com.localz.spotz.sdk.listeners;

public interface InitializationListener {
    void onInitialized();

    void onError(Exception exception);
}
