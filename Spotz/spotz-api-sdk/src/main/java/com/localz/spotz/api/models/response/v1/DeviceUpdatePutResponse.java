package com.localz.spotz.api.models.response.v1;

import com.google.gson.reflect.TypeToken;
import com.localz.spotz.api.models.Response;

import java.lang.reflect.Type;

public class DeviceUpdatePutResponse {
    public static final Type TYPE = new TypeToken<Response<DeviceUpdatePutResponse>>() {
    }.getType();

    public String deviceId;
    public String puid;
    public String auid;
    //public CustomerGetResponse customer;
}