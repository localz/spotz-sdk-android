package com.localz.spotz.sdk.listeners;

import com.localz.spotz.sdk.models.InitializedResponse;

public abstract class InitializationListenerAdapter implements InitializationListener {
    @Override
    public void onInitialized(InitializedResponse initializedResponse) {
    }

    @Override
    public void onDeviceNotRegistered() {
    }

    @Override
    public void onBluetoothDisabled() {
    }

    @Override
    public void onError(Exception exception) {
    }
}
