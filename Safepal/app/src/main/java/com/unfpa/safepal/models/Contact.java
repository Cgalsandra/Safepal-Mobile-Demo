package com.unfpa.safepal.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/29/17.
 */

public class Contact {
    @SerializedName("token")
    private String token;
    @SerializedName("caseNumber")
    private String caseNumber;
    @SerializedName("contact")
    private String contact;

    public Contact(String token, String caseNumber, String contact) {
        this.token = token;
        this.caseNumber = caseNumber;
        this.contact = contact;
    }
}
