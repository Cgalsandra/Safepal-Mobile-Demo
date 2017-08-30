package com.unfpa.safepal.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/29/17.
 */

public class Report {
    public Report(String token, String gender, String reporter, String incidentDate, String perpetuator, String age, Double latitude, Double longitude, String contact, String details, String reportSource, String reportDate, String reporterRelationship, String type) {
        this.token = token;
        this.gender = gender;
        this.reporter = reporter;
        this.incidentDate = incidentDate;
        this.perpetuator = perpetuator;
        this.age = age;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contact = contact;
        this.details = details;
        this.reportSource = reportSource;
        this.reportDate = reportDate;
        this.reporterRelationship = reporterRelationship;
        this.type = type;
    }

    @SerializedName("token")
    private String token;
    @SerializedName("gender")
    private String gender;
    @SerializedName("reporter")
    private String reporter;
    @SerializedName("incident_date")
    private String incidentDate;
    @SerializedName("perpetuator")
    private String perpetuator;
    @SerializedName("age")
    private String age;
    @SerializedName("latitude")
    private Double latitude;
    @SerializedName("longitude")
    private Double longitude;
    @SerializedName("contact")
    private String contact;
    @SerializedName("details")
    private String details;
    @SerializedName("report_source")
    private String reportSource;
    @SerializedName("reportDate")
    private String reportDate;
    @SerializedName("reporter_relationship")
    private String reporterRelationship;
    @SerializedName("type")
    private String type;


    public void setToken(String token) {
        this.token = token;
    }
}
