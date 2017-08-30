package com.unfpa.safepal.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by william on 7/29/17.
 */

public class ReportResponse {
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getCasenumber() {
        return casenumber;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("casenumber")
    @Expose
    private String casenumber;
    @SerializedName("csos")
    @Expose
    private List<Company> companies = null;
}
