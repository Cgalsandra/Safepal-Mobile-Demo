package com.unfpa.safepal.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/29/17.
 */

public class ContactResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
