package com.localz.spotz.api.models.response.v1.common;

import com.google.gson.annotations.SerializedName;

/**
 * Created by richardlay on 10/07/2014.
 */
public enum SubPropertyType {
    @SerializedName("RES")
    RESIDENTIAL,
    @SerializedName("COM")
    COMMERICAL,
    @SerializedName("CRP")
    CROPPING,
    @SerializedName("DRY")
    DAIRY,
    @SerializedName("FRM")
    FARMLET,
    @SerializedName("HOR")
    HORTICULTURE,
    @SerializedName("LIF")
    LIFESTYLE,
    @SerializedName("LIV")
    LIVESTOCK,
    @SerializedName("VIT")
    VITICULTURE,
    @SerializedName("MIX")
    MIXED_FARMING,
    @SerializedName("OTH")
    OTHER;
}
