package com.unfpa.safepal.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by william on 7/29/17.
 */

public class TokenResponse implements Serializable{
    @SerializedName("status")
    private String status;
    @SerializedName("token")
    private String token;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
