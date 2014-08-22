package com.localz.spotz.api.models.response.v1;

import com.google.gson.reflect.TypeToken;
import com.localz.spotz.api.models.Response;

import java.lang.reflect.Type;

public class BeaconsGetResponse {
    public static final Type TYPE = new TypeToken<Response<BeaconsGetResponse[]>>() {
    }.getType();

    public String spotzId;
    public String uuid;
    public Integer major;
    public Integer minor;
}