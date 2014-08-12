package com.localz.spotz.api.models.response.v1.common;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Media implements Serializable {
    public List<String> images;
    @SerializedName("vid")
    public String videoLink;
    @SerializedName("ext")
    public String externalLink;
}
