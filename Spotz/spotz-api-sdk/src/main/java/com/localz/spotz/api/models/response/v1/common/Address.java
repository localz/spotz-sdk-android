package com.localz.spotz.api.models.response.v1.common;

import java.io.Serializable;

public class Address implements Serializable {
    public String unitNo;
    public String streetNo;
    public String streetName;
    public String streetType;
    public String streetFull;
    public String city;
    public String state;
    public String zip;
    public Float[] location;
}
