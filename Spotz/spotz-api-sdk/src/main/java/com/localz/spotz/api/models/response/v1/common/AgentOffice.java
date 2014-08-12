package com.localz.spotz.api.models.response.v1.common;

import java.io.Serializable;

public class AgentOffice implements Serializable {
    public String id;
    public String agencyName;
    public String agencyCode;
    public String name;
    public String email;
    public String fax;
    public String phone;
    public String website;
    public Float[] location;
    public String officeType;
    public String licenseCode;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String zip;
    public String country;
    public String logoSmallUrl;
}
