package com.unfpa.safepal.report;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.unfpa.safepal.ProvideHelp.ContactFragment;
import com.unfpa.safepal.ProvideHelp.CsoActivity;
import com.unfpa.safepal.R;
import com.unfpa.safepal.service.Constant;
import com.unfpa.safepal.service.SafePalAPI;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportingActivity extends AppCompatActivity implements
        SurvivorIncidentFormFragment.OnFragmentInteractionListener,
        ContactFragment.OnFragmentInteractionListener,
        AnotherPersonIncidentFormFragment.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    //location
    public static double userLatitude =0;
    public static double userLongitude =0;
    private final static int PLAY_SERVICES_REQUEST = 1000;


    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;



    private Retrofit retrofit;
    String TAG = ReportingActivity.class.getSimpleName();
    /**
     * Next and buttonExit button
     */
    Button buttonNext;
    Button buttonExit;

   // public static double userLongitude =0;
    //public static double userLatitude =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reporting_toolbar);
        setSupportActionBar(toolbar);
        //update contact service

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(10 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        manageUI();

        loadWhoGetnHelpFragment();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        httpClient.addInterceptor(chain -> {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder().header("userid", Constant.USER_ID);
            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }).build();

        retrofit = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .baseUrl(Constant.END_POINT).build();


    }


    public static final int STATUS_SUBMIT_REPORT_SUBMITED = 0;
    public static final int STATUS_SUBMIT_REPORT_ERROR = 1;
    public static final int STATUS_SUBMIT_REPORT_ALREADY_AVAILABLE = 2;

    private void manageUI() {
        //look for vies
        //Abort fab of  who's getting help activity
        buttonExit = (Button) findViewById(R.id.exit_app);
        //Next fab of  who's getting help activity
        buttonNext = (Button) findViewById(R.id.finish);

        //set listerns
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "button next clicked" );
                if (isFragmentVisible(getFragmentManager().findFragmentByTag(
                        WhoSGettingHelpFragment.class.getSimpleName()))) {//cuurent frag WhoSGettingHelpFragment
                    if (WhoSGettingHelpFragment.wsghYesRB.isChecked()) {//happened to me
                        //Log.d(TAG, "loading reporting fragment for self");
                        loadReportingFormSelfFragment();//used in the WHoIsGettingHelp Fragment
                        updateNextButtonToSubmit();
                    } else if (WhoSGettingHelpFragment.wsghSomeelseRb.isChecked()) {//happened to someone else
                        if (WhoSGettingHelpFragment.wsghRelationshipSpinner.getSelectedItemPosition() <= 0) {
                            WhoSGettingHelpFragment.wsghFeedbackSnackbar = Snackbar.make(view, "what is your relationship to survivor?", Snackbar.LENGTH_LONG);
                            WhoSGettingHelpFragment.wsghFeedbackSnackbar.show();
                        } else {
                            //Log.d(TAG, "loading reporting fragment for happeed to someone else");
                            loadReportingFormSomeOneElseFragment();
                            updateNextButtonToSubmit();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Who did the incident happen to? Choose one of the options to proceed.", Toast.LENGTH_LONG).show();
                    }

                } else if (isFragmentVisible(getFragmentManager().findFragmentByTag(
                        AnotherPersonIncidentFormFragment.class.getSimpleName()))) {//cuurent frag AnotherPersonIncidentFormFragment

                    Log.d(TAG, "submitting another-person form");
                    int status = AnotherPersonIncidentFormFragment.submitForm(getBaseContext());//submit the form

                    if ((status == ReportingActivity.STATUS_SUBMIT_REPORT_SUBMITED) || (status == ReportingActivity.STATUS_SUBMIT_REPORT_ALREADY_AVAILABLE)) {

                        Intent csoIntent = new Intent(getBaseContext(), CsoActivity.class);
                        startActivity(csoIntent);
                        finish();//close this activity after opening another.

                    } else {
                        Log.d(TAG, "error in data????");
                    }

                } else if (isFragmentVisible(getFragmentManager().findFragmentByTag(
                        SurvivorIncidentFormFragment.class.getSimpleName()))) {//cuurent frag SurvivorIncidentFormFragment
                    Log.d(TAG, "submitting self-form");
                    int status = SurvivorIncidentFormFragment.submitForm(getBaseContext());//submit the form
                    if ((status == ReportingActivity.STATUS_SUBMIT_REPORT_SUBMITED) || (status == ReportingActivity.STATUS_SUBMIT_REPORT_ALREADY_AVAILABLE)) {

                        Intent csoIntent = new Intent(getBaseContext(), CsoActivity.class);
                        startActivity(csoIntent);
                        finish();//close this activity after opening another.


                        Log.d(TAG, "SurvivorIncidentFormFragment.submitForm successfull. Loading contact frag");
                    } else {
                        Log.d(TAG, "errpr on data????");
                    }

                } else if (isFragmentVisible(getFragmentManager().findFragmentByTag(
                        ContactFragment.class.getSimpleName()))) {//cuurent frag ContactFragment

                    if (ContactFragment.areFieldsSet(getBaseContext())) {//if all foed are set
                        Log.d("Code", "reached");
//                        updateNetworkContat();

                        Intent csoIntent = new Intent(getBaseContext(), CsoActivity.class);
                        startActivity(csoIntent);
                        finish();//close this activity after opening another.
                    } else {
                        Log.w(TAG, "some fields empty");
                    }

                } else {
                    Log.e(TAG, "Dont know what to do!!!!");
                }
            }
        });

        //exit the  application on click of exit
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                Process.killProcess(Process.myPid());
                System.exit(2);
            }
        });

        //unistall application on long press of exit
        buttonExit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Uri packageURI = Uri.parse("package:com.unfpa.safepal");
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                startActivity(uninstallIntent);

                return true;
            }
        });

    }

    /**
     * changes text fron 'NEXT' to 'SUBMIT'
     * while on the last frgamnet of reporting an incident
     */
    protected void updateNextButtonToSubmit() {
        buttonNext.setText(getString(R.string.submit));
    }

    /**
     * changes text fron 'SUBMIT' to 'NEXT'
     */
    protected void updateSubmitButtonToNext() {
        buttonNext.setText(getString(R.string.next));
    }

    /**
     * Loads fragment with form for submitting details about someone else who has
     * suffered violence
     */
    public void loadReportingFormSomeOneElseFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AnotherPersonIncidentFormFragment fragment = AnotherPersonIncidentFormFragment
                .newInstance(WhoSGettingHelpFragment.wsghRelationshipSpinner.getSelectedItem().toString(), "UNUSED");
        if (isFragmentVisible(fragment)) {
            Log.d(TAG, "AnotherPersonIncidentFormFragment is already visible, not reforming another...");
        } else {
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            fragmentTransaction.replace(R.id.fragment_container, fragment, AnotherPersonIncidentFormFragment.class.getSimpleName());
            fragmentTransaction.commit();
            Log.d(TAG, "loaded 'AnotherPersonIncidentFormFragment' fragment");
        }
    }

    /**
     * loads reporting for for the survivir him self
     */
    private void loadReportingFormSelfFragment() {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //SurvivorIncidentFormFragment fragment = new SurvivorIncidentFormFragment();
        SurvivorIncidentFormFragment fragment = SurvivorIncidentFormFragment
                .newInstance("UNUSED", "UNUSED");
        if (isFragmentVisible(fragment)) {
            Log.d(TAG, "SurvivorIncidentFormFragment is already visible, not reforming another...");
        } else {
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            fragmentTransaction.replace(R.id.fragment_container, fragment, SurvivorIncidentFormFragment.class.getSimpleName());
            fragmentTransaction.commit();
            Log.d(TAG, "loaded 'SurvivorIncidentFormFragment' fragment!");
        }
    }

    /**
     * loads fragment for choosing who survived the incodent.
     * The survir gim self or someone else
     */
    private void loadWhoGetnHelpFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        WhoSGettingHelpFragment fragment = new WhoSGettingHelpFragment();
        if (isFragmentVisible(fragment)) {
            Log.d(TAG, "WhoSGettingHelpFragment is already visible, not reforming another...");
        } else {
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            fragmentTransaction.replace(R.id.fragment_container, fragment, WhoSGettingHelpFragment.class.getSimpleName());
            fragmentTransaction.commit();
            Log.d(TAG, "loaded 'Who-is-getting-help' fragment");
        }


    }



    private boolean isFragmentVisible(Fragment fragment) {
        if ((fragment != null) &&
                fragment.isVisible()) {
            return true;
        } else return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        userLatitude = location.getLatitude() ;

        userLongitude = location.getLongitude() ;
        Log.d("lat:lng", Double.toString(location.getLatitude())+":"+Double.toString(location.getLongitude()));

        //Toast.makeText(getApplicationContext(),Double.toString(location.getLatitude())+":"+Double.toString(location.getLongitude()), Toast.LENGTH_LONG ).show();
    }

    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(googleApiClient, getIndexApiAction());
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (googleApiClient != null && googleApiClient.isConnected()) {
          // googleApiClient.disconnect();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(googleApiClient.isConnected())
            requestLocationUpdates();

    }
    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        if(googleApiClient != null && googleApiClient.isConnected()){
        AppIndex.AppIndexApi.end(googleApiClient, getIndexApiAction());
        googleApiClient.disconnect();}
    }





    public static Address getFullAddress(Context context, double latitude,double longitude)
    {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    public boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this,resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.",Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Reporting Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}





