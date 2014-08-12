package com.localz.spotz.api.models.response.v1.common;

import com.google.gson.annotations.SerializedName;

/**
 * Created by richardlay on 10/07/2014.
 */
public enum PropertyType {
    @SerializedName("HSE")
    HOUSE("House"),
    @SerializedName("APT")
    APARTMENT("Apartment"),
    @SerializedName("UNI")
    UNIT("Unit"),
    @SerializedName("ACR")
    ACREAGE_SEMIRURAL("Acreage/semi rural"),
    @SerializedName("TWN")
    TOWNHOUSE("Townhouse"),
    @SerializedName("VIL")
    VILLA("Villa"),
    @SerializedName("TER")
    TERRACE("Terrace"),
    @SerializedName("STD")
    STUDIO("Studio"),
    @SerializedName("BOU")
    BLOCK_OF_UNITS("Block of units"),
    @SerializedName("FLT")
    FLAT("Flat"),
    @SerializedName("SRV")
    SERVICED_APARTMENT("Serviced apartment"),
    @SerializedName("LAN")
    LAND("Land"),
    @SerializedName("RUR")
    RURAL("Rural"),
    @SerializedName("OTH")
    OTHER("Other");

    private final String description;

    private PropertyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
