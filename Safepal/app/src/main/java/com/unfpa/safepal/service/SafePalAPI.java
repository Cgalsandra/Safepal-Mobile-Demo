package com.unfpa.safepal.service;

import com.unfpa.safepal.models.Report;
import com.unfpa.safepal.models.ReportResponse;
import com.unfpa.safepal.models.TokenResponse;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by william on 7/29/17.
 */

public interface SafePalAPI {
    @GET("auth/newtoken")
    Observable<TokenResponse> getToken();

    @POST("reports/addreport")
    Observable<ReportResponse> addReport(@Body Report report);

}
