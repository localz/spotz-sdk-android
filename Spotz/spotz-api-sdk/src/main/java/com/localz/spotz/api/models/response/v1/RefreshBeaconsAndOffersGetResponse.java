package com.localz.spotz.api.models.response.v1;

import com.google.gson.reflect.TypeToken;
import com.localz.spotz.api.models.Response;

import java.lang.reflect.Type;

public class RefreshBeaconsAndOffersGetResponse {
    public static final Type TYPE = new TypeToken<Response<RefreshBeaconsAndOffersGetResponse>>() {
    }.getType();

    public String name;
    public String tag;
    public String auid;
}