package com.unfpa.safepal.network;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.unfpa.safepal.models.Report;
import com.unfpa.safepal.models.ReportResponse;
import com.unfpa.safepal.models.TokenResponse;
import com.unfpa.safepal.service.Constant;
import com.unfpa.safepal.service.SafePalAPI;
import com.unfpa.safepal.store.ReportIncidentContentProvider;
import com.unfpa.safepal.store.ReportIncidentTable;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Kisa on 10/8/2016.
 */

public class AddReportService extends IntentService {
    private Retrofit retrofit;
    private SafePalAPI safePalAPI;


    /**
    /**
     * An IntentService must always have a constructor that calls the super constructor. The
     * string supplied to the super constructor is used to give a name to the IntentService's
     * background thread.
     */
    public AddReportService() {
        super("AddReportService");
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        httpClient.addInterceptor(chain -> {
            okhttp3.Request originalRequest = chain.request();
            okhttp3.Request.Builder builder = originalRequest.newBuilder().header("userid", Constant.USER_ID);
            okhttp3.Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }).build();

        retrofit = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .baseUrl(Constant.END_POINT).build();
        safePalAPI = retrofit.create(SafePalAPI.class);
    }

    /**
     * In an IntentService, onHandleIntent is run on a background thread.  As it
     * runs, it broadcasts its current status using the LocalBroadcastManager.
     * @param workIntent The Intent that starts the IntentService. This Intent contains the
     * URL of the web site from which the RSS parser gets data.
     */

    @Override
    protected void onHandleIntent(Intent workIntent) {

        // Gets a URL to read from the incoming Intent's "data" value
        String localUrlString = workIntent.getDataString();

        // A cursor that's local to this method.

        /*
         * A block that tries to connect to the Picasa featured picture URL passed as the "data"
         * value in the incoming Intent. The block throws exceptions (see the end of the block).
         */

        Cursor cursor =  getContentResolver().query(
                ReportIncidentContentProvider.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (cursor != null) {
            cursor.moveToLast();
            Observable<TokenResponse> response = getTokenFromServer();
            response.subscribe(new Subscriber<TokenResponse>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(TokenResponse tokenResponse) {
                    Log.d("REPORT_SEND", "GOT AUTH TOKEN");
                    Report report = createReport(tokenResponse.getToken(),
                            cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_SURVIVOR_GENDER)),
                            cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTED_BY)),
                            "","Unknown",
                            cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_SURVIVOR_DATE_OF_BIRTH)),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_LOCATION_LAT))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_LOCATION_LNG))),
                            cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_PHONE_NUMBER)),
                            cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_INCIDENT_STORY)),
                            "android user","",
                            cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_INCIDENT_TYPE)));
                    Log.d("REPORT_SEND", "CREATED MY REPORT");

                    Observable<ReportResponse> resp = safePalAPI.addReport(report).
                            subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                    resp.subscribe(new Subscriber<ReportResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("REPORT_SEND", e.getMessage());
                        }

                        @Override
                        public void onNext(ReportResponse reportResponse) {
                            Log.d("REPORT_SEND", "REPORT SENT");
                            Cursor cursorUpdate =  getContentResolver().query(
                                    ReportIncidentContentProvider.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null);
                            Log.d("REPORT_SEND", "GOT CONTENT FROM DB");
                            ContentValues dataValues = new ContentValues();
                            dataValues.put(ReportIncidentTable.COLUMN_UNIQUE_IDENTIFIER, reportResponse.getCasenumber());
                            Toast.makeText(getBaseContext(), " The SafePal No." + reportResponse.getCasenumber(),Toast.LENGTH_SHORT).show();

                            if (cursorUpdate != null) {
                                cursorUpdate.moveToLast();

                                // Update reported incident
                                getContentResolver().update(ReportIncidentContentProvider.CONTENT_URI, dataValues, ReportIncidentTable.COLUMN_ID + "=" +
                                        cursorUpdate.getString(cursorUpdate.getColumnIndex(
                                                ReportIncidentTable.COLUMN_ID)), null);

                            }
                            cursor.close();
                        }
                    });

                }
            });

//            sendReportToServer(
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_SURVIVOR_GENDER)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_SURVIVOR_DATE_OF_BIRTH)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_INCIDENT_TYPE)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_INCIDENT_STORY)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTED_BY)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_LOCATION_LAT)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_LOCATION_LNG)),
//                    cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_PHONE_NUMBER)),
//                    localUrlString,
//
//                    new VolleyCallback() {
//
//                        @Override
//                        public void onSuccessResponse(String result) {
//
//                          //  Log.d("result", result);
//
//                            try {
//                                JSONObject response = new JSONObject(result);
//                               // Log.d("",response.toString());
//                                Cursor cursorUpdate =  getContentResolver().query(
//                                        ReportIncidentContentProvider.CONTENT_URI,
//                                        null,
//                                        null,
//                                        null,
//                                        null);
//                                ContentValues dataValues = new ContentValues();
//                                dataValues.put(ReportIncidentTable.COLUMN_UNIQUE_IDENTIFIER, response.getString("casenumber"));
//                                Toast.makeText(getBaseContext(), " The SafePal No." + response.getString("casenumber"),Toast.LENGTH_SHORT).show();
//
//                                if (cursorUpdate != null) {
//                                    cursorUpdate.moveToLast();
//
//                                    // Update reported incident
//                                    getContentResolver().update(ReportIncidentContentProvider.CONTENT_URI, dataValues, ReportIncidentTable.COLUMN_ID + "=" +
//                                            cursorUpdate.getString(cursorUpdate.getColumnIndex(
//                                                    ReportIncidentTable.COLUMN_ID)), null);
//
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//
//
//                        }
//
//                    }
//
//            );

        }

    }

    public Observable<TokenResponse> getTokenFromServer(){
        return safePalAPI.getToken().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Report createReport(String token, String gender, String reporter, String incidentDate,
                               String perpetrator, String age, Double latitude, Double longitude,
                               String contact, String details, String reportSource,
                               String reporterRelationShip, String type){
        final String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return new Report(token, gender, reporter, incidentDate, perpetrator, age, latitude, longitude,
                contact, details, reportSource, currentDate, reporterRelationShip, type);

    }


//    public void sendReportToServer(
//                                   final String toServerSGender,
//                                   final String toServerSDOB,
//                                   final String toServerIType,
//                                   final String toServerIDescription,
//                                   final String toServerReportedBy,
//
//                                   final String toServerReporterLat,
//                                   final String toServerReportedLng,
//                                   final String toServerReporterPhonenumber,
//                                   final String addReportUrl, final VolleyCallback reportCallback ){
//
//
//
//        final String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//
//
//        getTokenFromServer(new VolleyCallback() {
//            @Override
//            public void onSuccessResponse(String tokenResponse) {
//                try {
//                    JSONObject tokenObject = new JSONObject(tokenResponse);
//                    final  String  serverReceivedToken = tokenObject.getString("token");
//
//                    // This volley request sends a report to the server with the received token
//                    StringRequest addReportRequest = new StringRequest(Request.Method.POST, addReportUrl,
//                            new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String addReportReponse) {
//                                    reportCallback.onSuccessResponse(addReportReponse);
//                                }
//                            },
//                            new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    Log.d("Not Submitted", error.getMessage());
//                                }
//                            }){
//
//                        @Override
//                        protected Map<String, String> getParams() throws AuthFailureError {
//                            HashMap<String, String> addReport = new HashMap<String, String>();
//
//                            addReport.put("token", serverReceivedToken);
//                            addReport.put("type",toServerIType);
//                            addReport.put("gender",toServerSGender);
//                            addReport.put("reporter",toServerReportedBy);
//                            addReport.put("incident_date","null");
//                            addReport.put("perpetuator","Unknown");
//                            addReport.put("age",toServerSDOB);
//                            addReport.put("contact",toServerReporterPhonenumber);
//                            addReport.put("latitude",toServerReporterLat);
//                            addReport.put("longitude",toServerReportedLng);
//                            addReport.put("details",toServerIDescription);
//                            addReport.put("report_source","android user");
//                            addReport.put("reportDate",currentDate);
//                            return addReport;                        }
//
//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            HashMap<String, String> addReportHeaders = new HashMap<String, String>();
//                            addReportHeaders.put("userid", "C7rPaEAN9NpPGR8e9wz9bzw");
//                            return  addReportHeaders;
//                        }
//
//                    };
//
//
//                    MySingleton.getInstance(getApplicationContext()).addToRequestQueue(addReportRequest);
//
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        });
//    }
//
//

//    private void getTokenFromServer(final VolleyCallback tokenCallback) {
//
//
////        final String tokenUrl = " https://api-safepal.herokuapp.com/index.php/api/v1/auth/newtoken";
//        final String tokenUrl = "https://api.safepal.co/api/v1/auth/newtoken";
//
//        // This volley request gets a token from the server
//        StringRequest tokenRequest = new StringRequest(Request.Method.GET, tokenUrl,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String tokenResponse) {
//                        tokenCallback.onSuccessResponse(tokenResponse);
//
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("Failed to get token", error.getMessage());
//                    }
//                }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("userid", "C7rPaEAN9NpPGR8e9wz9bzw");
//
//                return headers;
//            }
//        };
//        //add request to queue
//
//        MySingleton.getInstance(this).addToRequestQueue(tokenRequest);
//
//    }

}
