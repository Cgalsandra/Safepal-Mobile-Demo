package com.unfpa.safepal.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/29/17.
 */

public class Company {
    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public String getCompanyLocation() {
        return companyLocation;
    }

    public String getCompanyLatitude() {
        return companyLatitude;
    }

    public String getCompanyLongitude() {
        return companyLongitude;
    }

    public String getCompanyWorkingHours() {
        return companyWorkingHours;
    }

    public String getCompanyPhoneNumber() {
        return companyPhoneNumber;
    }

    public String getCompanyDetailsId() {
        return companyDetailsId;
    }

    @SerializedName("cso_name")
    @Expose
    private String companyName;
    @SerializedName("cso_email")
    @Expose
    private String companyEmail;
    @SerializedName("cso_location")
    @Expose
    private String companyLocation;
    @SerializedName("cso_latitude")
    @Expose
    private String companyLatitude;
    @SerializedName("cso_longitude")
    @Expose
    private String companyLongitude;
    @SerializedName("cso_working_hours")
    @Expose
    private String companyWorkingHours;
    @SerializedName("cso_phone_number")
    @Expose
    private String companyPhoneNumber;
    @SerializedName("cso_details_id")
    @Expose
    private String companyDetailsId;
}
