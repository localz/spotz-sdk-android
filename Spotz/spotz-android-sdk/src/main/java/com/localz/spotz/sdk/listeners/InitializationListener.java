package com.localz.spotz.sdk.listeners;

import com.localz.spotz.sdk.models.InitializedResponse;

public interface InitializationListener {
    void onInitialized(InitializedResponse initializedResponse);

    void onDeviceNotRegistered();

    void onBluetoothDisabled();

    void onError(Exception exception);
}
