package com.unfpa.safepal.ProvideHelp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.unfpa.safepal.ProvideHelp.RVCsoModel.BeforeCsoInfo;
import com.unfpa.safepal.ProvideHelp.RVCsoModel.CsoRvAdapter;
import com.unfpa.safepal.ProvideHelp.RVCsoModel.TheCSO;
import com.unfpa.safepal.R;
import com.unfpa.safepal.messages.EMessageDialogFragment;
import com.unfpa.safepal.store.RIContentObserver;
import com.unfpa.safepal.store.ReportIncidentContentProvider;
import com.unfpa.safepal.store.ReportIncidentTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.unfpa.safepal.report.WhoSGettingHelpFragment.randMessageIndex;

public class CsoActivity extends AppCompatActivity {
    //calling permission code
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;

    /**
     * Next and buttonExit button
     */
    Button buttonNext;
    Button buttonExit;



    TextView csoSafepalNo, csoContactInfo,csoAssuranceHelp, csoEncouragingMessagesTV;

    //variables for the nearest cso list

    private List<BeforeCsoInfo> beforeCsoList = new ArrayList<>();
    private List<TheCSO> csosList = new ArrayList<>();

    private RecyclerView csosRecyclerView;
    private CsoRvAdapter csosAdapter;

    //no internet connection
    private ProgressBar csoProgressBar;
    private LinearLayout csoNoInternetLL;
    private Button csoNoInternetButton;
    // TheCSOs json url
    private static final String URL_CSO_API = "http://52.43.152.73/api/location.php";
     //This is a temporary list of cso's hard coded here


     /**
     * Represents a geographical location.
     */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cso);



        //buttonFinish and buttonExit buttons
        buttonNext = (Button) findViewById(R.id.finish);
        buttonExit = (Button) findViewById(R.id.exit_app);

        csoNoInternetButton = (Button)findViewById(R.id.cso_no_internet_button);
        csoNoInternetLL = (LinearLayout)findViewById(R.id.cso_no_internet_ll);

        // choose someone else relationship spinner
        csoEncouragingMessagesTV = (TextView) findViewById(R.id.cso_ecouraging_messages_tv);
        csoSafepalNo = (TextView)findViewById(R.id.cso_safepal_number);
        csoContactInfo= (TextView)findViewById(R.id.cso_contact_info);
        csoAssuranceHelp = (TextView)findViewById(R.id.cso_assurance_help);

        Toolbar csoToolbar = (Toolbar) findViewById(R.id.cso_toolbar);
        setSupportActionBar(csoToolbar);


        loadCsoMessages();

        updateCsoUIDTV();


        csosRecyclerView = (RecyclerView) findViewById(R.id.cso_recycler_view);

        csosAdapter = new CsoRvAdapter(csosList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        csosRecyclerView.setLayoutManager(mLayoutManager);
        csosRecyclerView.setItemAnimator(new DefaultItemAnimator());
        csosRecyclerView.setAdapter(csosAdapter);
        updateUserWithCsos();

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTaskToBack(true);
                //Process.killProcess(Process.myPid());
               System.exit(1);


            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "button next clicked");
               finish();

            }
        });
    }
     String TAG = CsoActivity.class.getSimpleName();
    //Randomly load encouraging messages to the Text View
     public void loadCsoMessages() {
        String[] csoMessagesArray = getResources().getStringArray(R.array.signs_of_sgbv);
        csoEncouragingMessagesTV.setText(csoMessagesArray[randMessageIndex(0, csoMessagesArray.length)].toString());
    }

    //shows encouraging messages in dialog on click of the Text View
    public void onClickCsoEncouragingMessages(View view) {

        EMessageDialogFragment emDialog = EMessageDialogFragment.newInstance(
                getString(R.string.signs_of_sgbv_header),
                csoEncouragingMessagesTV.getText().toString(),
                getString(R.string.close_dialog));
        emDialog.show(getFragmentManager(), "encouraging message");
    }

    public void onClickCsoCall(View view) {

        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            String dial = "tel:116" ;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        } else {
            //Toast.makeText(this, "Permission Call Phone denied", Toast.LENGTH_SHORT).show();
            if (checkPermission(Manifest.permission.CALL_PHONE)) {
                //dial.setEnabled(true);
            } else {
                //dial.setEnabled(false);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // Method pushes the data to json server suing volley
    private void getNearestCSOs(String getLat, String getLong) {


        beforeCsoList.add(new BeforeCsoInfo("Reproductive Health Uganda, Kamokya",0.337464,32.582272,"+256312207100"));
        beforeCsoList.add(new BeforeCsoInfo("Naguru Teenage Center , Bugolobi",0.320989,32.617236,"0800112222"));
        beforeCsoList.add(new BeforeCsoInfo("Fida, Kira Road",0.348204,32.596336,"+256414530848"));
        beforeCsoList.add(new BeforeCsoInfo("Action Aid , Sir Apollo Rd",0.342041,32.562558,"+256414510363"));


        for(int i =0 ; i<beforeCsoList.size(); i++){
           if(getLat.equalsIgnoreCase("0.0")  || getLong =="0.0"){
               csosList.add(new TheCSO(beforeCsoList.get(i).getBefore_cso_name(), "We failed to locate you", beforeCsoList.get(i).getBefore_cso_phonenumber()));
           }
            else{
               String disBetweenCso = String.format("%.2f", geographicalDistance(
                       Double.parseDouble(getLat),
                       Double.parseDouble(getLong),
                       beforeCsoList.get(i).getBefore_cso_lat(),
                       beforeCsoList.get(i).getBefore_cso_long()));

               Log.d("location from db", getLat +":" + getLong);
               csosList.add(new TheCSO(beforeCsoList.get(i).getBefore_cso_name(), disBetweenCso + "Km away from you", beforeCsoList.get(i).getBefore_cso_phonenumber()));
           }
            Collections.sort(csosList, new Comparator<TheCSO>() {
                @Override
                public int compare(TheCSO o1, TheCSO o2) {
                    return o1.getCso_distance().compareTo(o2.getCso_distance());
                }
            });
           }



        csosAdapter.notifyDataSetChanged();

    }


    /** All the  Methods **/
    //updates safepal number
    public void updateCsoUIDTV(){
        Cursor cursor =  getContentResolver().query(
                ReportIncidentContentProvider.CONTENT_URI,
                null,
                null,
                null,
                null);
        if(cursor != null) {
            StringBuilder offline = new StringBuilder();
            cursor.moveToLast();
            offline.append("Your SafePal Number is: " + cursor.getString(cursor.getColumnIndex(ReportIncidentTable.COLUMN_UNIQUE_IDENTIFIER)));


            csoSafepalNo.setText(offline);
        }
        cursor.close();

        Handler riHandler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        String sb = (String) msg.obj;
                        csoSafepalNo.setText(sb);
                        break;

                    default:

                        break;
                }
            };
        };

        RIContentObserver rICsoContentObserver = new RIContentObserver(this, riHandler);

        getContentResolver().registerContentObserver(ReportIncidentContentProvider.CONTENT_URI,
                true,
                rICsoContentObserver);


    }
    //retrieves lat and lng from db and inserts them into the remote method for retreiving the csos
    public void updateUserWithCsos(){

        Cursor cursorRetrieveLatLng =  getContentResolver().query(
                ReportIncidentContentProvider.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursorRetrieveLatLng != null) {

            cursorRetrieveLatLng.moveToLast();
            String dbLatString =  cursorRetrieveLatLng.getString(cursorRetrieveLatLng.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_LOCATION_LAT));
            String dbLngString =  cursorRetrieveLatLng.getString(cursorRetrieveLatLng.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_LOCATION_LNG));
            String dbPhoneString =  cursorRetrieveLatLng.getString(cursorRetrieveLatLng.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_PHONE_NUMBER));
            String dbEmailString =  cursorRetrieveLatLng.getString(cursorRetrieveLatLng.getColumnIndex(ReportIncidentTable.COLUMN_REPORTER_EMAIL));

            if(dbPhoneString.length()>8){
                csoContactInfo.setText("Contact Phonenumber: " + dbPhoneString);
                csoAssuranceHelp.setText("Safepal will contact you on the above phonenumber.");
                if(dbEmailString.length()>8){
                    csoContactInfo.setText("Contact Phonenumber: " + dbPhoneString+ "\nContact Email: " +dbEmailString);
                    csoAssuranceHelp.setText("Safepal will contact you on the above phonenumber or email. "); }
            }
            else {
                csoContactInfo.setText("No Contacts provided. " );
                csoAssuranceHelp.setText("Since you did not provide a contact number, safepal service providers will not be able to contact you directly. But you can still walk in to any of the service providers below with your safepal number and they will attend to you. ");

            }

            getNearestCSOs(dbLatString,dbLngString);
            Log.d("cso lat and long", dbLatString+"- : -"+dbLngString);
        }
        cursorRetrieveLatLng.close();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cso, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_cso_guide:
                csoGuide();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void csoGuide(){
        ViewTarget eTarget = new ViewTarget(R.id.cso_childhelpline_btn, this);
        ShowcaseView homeExitSv = new ShowcaseView.Builder(this)
                .withHoloShowcase()
                .setTarget(eTarget)
                .setContentTitle("Talk to someone")
                .setContentText("Click on the call button in order to talk to someone for help")
                .setStyle(R.style.ExitShowcaseTheme)
                .build();

    }

    private double geographicalDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



    //calling permission
    private  boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
   //calling permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                   // dial.setEnabled(true);
                    Toast.makeText(this, "You can call the number by clicking on the button", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


}

