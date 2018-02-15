package com.unfpa.safepal.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.unfpa.safepal.R;
import com.unfpa.safepal.Utils.General;
import com.unfpa.safepal.messages.EMessageDialogFragment;
import com.unfpa.safepal.report.ReportingActivity;

import io.fabric.sdk.android.Fabric;
import java.util.Random;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity {

    //Global Variables 1234
    /**
     * Next and buttonExit button
     */
    private static final int PERMISSION_REQUEST_CODE = 200;
   // private View view;


    FloatingActionButton fabReportCase;
    Button buttonExit;
    Button buttonNext;
    RelativeLayout infoPanel;
    TextView textViewMessage;
    AppCompatCheckBox checkBoxAutoScroll;

    //guide for safepal
    ShowcaseView homeReportGuideSv, homeExitSv, homeNextSv;
    RelativeLayout.LayoutParams lps, nextLps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        // Assignments of variables
        buttonExit = (Button) findViewById(R.id.exit_app);
        buttonNext = (Button) findViewById(R.id.home_next_message);
        fabReportCase = (FloatingActionButton) findViewById(R.id.fab_report_incident);
        infoPanel = (RelativeLayout) findViewById(R.id.home_info_panel);
        textViewMessage = (TextView) findViewById(R.id.message);
        checkBoxAutoScroll = (AppCompatCheckBox) findViewById(R.id.auto_scroll_CheckBox);


        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 21) finishAndRemoveTask();
                else finish();
            }
        });
        buttonExit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Uri packageURI = Uri.parse("package:com.unfpa.safepal");
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                startActivity(uninstallIntent);

                return true;
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                animateNextMessage();
               // getTokenFromServer();
            }
        });

        fabReportCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkLocationPermission()){
                   // Log.d("Status", "permission checked");
                    // starts the reporting if location is granted
               // startActivity(new Intent(getApplicationContext(), ReportingActivity.class));}
                  openReporting();
                }
                else{

                    requestLocationPermission();

                }
            }
        });

        //for internal support
        checkBoxAutoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    activateAutoScrollTimer();
                    Log.d(TAG, "activated timer");
                } else {
                    Log.d(TAG, "deactivated timer");
                    deactivateAutoScrollTimer();
                }
            }
        });


        //animations about messages
        animSlideIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.enter_from_right);
        animExit = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.exit_to_left);
        animExit.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateMessageText();
                infoPanel.startAnimation(animSlideIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animateNextMessage();//show first message
        showDisclaimer();

    }

    private void showDisclaimer() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_name), MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(getString(R.string.first_time), true);
        if (isFirstTime) {
            homeReportGuide();
            General.showDisclaimerDialog(this);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getString(R.string.first_time), false);
            editor.apply();//indicate that app has ever been opened
        } else {
            //General.showDisclaimerDialog(this);
        }
    }

    Animation animSlideIn;
    Animation animExit;

    void animateNextMessage() {
        infoPanel.startAnimation(animExit);
    }

    boolean isAutoScrollOn = true;
    Thread threadScrolling;

    private void activateAutoScrollTimer() {
        isAutoScrollOn = true;
        threadScrolling = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isAutoScrollOn) {
                    try {
                        Log.d(TAG, "loop!");
                        int timeOut = getResources().getInteger(R.integer.message_timeout);
                        Log.d(TAG, "timeout: " + timeOut);
                        Thread.sleep(timeOut);//wait a bit
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                animateNextMessage();//change message
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "error: " + e.toString());
                    }
                }
                Log.d(TAG, "done n thread");
            }
        });
        threadScrolling.start();

    }

    private void deactivateAutoScrollTimer() {
        isAutoScrollOn = false;

    }

    String TAG = HomeActivity.class.getSimpleName();

    private void updateMessageText() {//// TODO: 13-Nov-16 dynamically set messages
        //make adapter
        ArrayAdapter<CharSequence> messages = ArrayAdapter.createFromResource(this,
                R.array.home_contact_info, R.layout.spinner_item);//// TODO: 14-Nov-16 Is this the correct Array???
        Random random = new Random();
        int min = 0;
        int max = messages.getCount();
        Log.d(TAG, "max: " + max);
        int randomIndex = random.nextInt(max - min) + min;
        Log.d(TAG, "randomIndex: " + randomIndex);
        String msg = messages.getItem(randomIndex).toString();
        Log.d(TAG, "textViewMessage: " + msg);
        textViewMessage.setText(msg);

    }

/*
    //expand encouraging messages
    public void onClickInfoPopUp(View view) {
        EMessageDialogFragment emDialog = EMessageDialogFragment.newInstance(
                "Safepal",
                textViewMessage.getText().toString(),
                getString(R.string.close_dialog));
        emDialog.show(getFragmentManager(), "encouraging message");
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_disclaimer:

                General.showDisclaimerDialog(this);
                return true;
            case R.id.menu_guide:
                homeReportGuide();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //This is a tutorial for the first  time reporters
    public void homeReportGuide() {

        //guide for the first time users
        lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        ViewTarget target = new ViewTarget(R.id.fab_report_incident, this);
        homeReportGuideSv = new ShowcaseView.Builder(this)
                .withHoloShowcase()
                .setTarget(target)
                .setContentTitle(R.string.home_guide_fab_report_title)
                .setContentText(R.string.home_guide_fab_report_text)
                .setStyle(R.style.ReportShowcaseTheme)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        homeReportGuideSv.hide();
                        homeExitGuide();
                    }
                })
                .build();
        homeReportGuideSv.setButtonPosition(lps);
    }

    public void homeExitGuide() {


        ViewTarget eTarget = new ViewTarget(R.id.exit_app, HomeActivity.this);
        homeExitSv = new ShowcaseView.Builder(HomeActivity.this)
                .withHoloShowcase()
                .setTarget(eTarget)
                .setContentTitle(R.string.home_guide_fab_exit_title)
                .setContentText(R.string.home_guide_fab_exit_text)
                .setStyle(R.style.ExitShowcaseTheme)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        homeExitSv.hide();
                        homeNextGuide();

                    }
                })
                .build();

    }

    public void homeNextGuide() {
      //guide for the first time users
        nextLps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nextLps.addRule(RelativeLayout.CENTER_IN_PARENT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        nextLps.setMargins(margin, margin, margin, margin);

        ViewTarget nTarget = new ViewTarget(R.id.home_next_message, HomeActivity.this);

        homeNextSv = new ShowcaseView.Builder(HomeActivity.this)
                .withHoloShowcase()
                .setTarget(nTarget)
                .setContentTitle(R.string.home_guide_fab_next_title)
                .setContentText(R.string.home_guide_fab_next_text)
                .setStyle(R.style.NextShowcaseTheme)
                .build();

        homeNextSv.setButtonPosition(nextLps);
        homeNextSv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeNextSv.hide();
            }
        });


    }


    //check for the permission
    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    //request for the permission
    private void requestLocationPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean locationCoarseAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && locationCoarseAccepted)
                       // Snackbar.make(view, "Permission Granted, Now you can access location data and camera.", Snackbar.LENGTH_LONG).show();
                        // starts the reporting if location is granted
                        startActivity(new Intent(getApplicationContext(), ReportingActivity.class));

                    else {

                       // Snackbar.make(view, "Permission Denied, You cannot access location data and camera.", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to location permission in order to report cases with safepal",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                        //version less than Marshmallow
                        else  {
                            openReporting();
                            Log.d("Status", "Handles location for android < version Marshmallow ");
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void openReporting(){
        startActivity(new Intent(getApplicationContext(), ReportingActivity.class));
    }
}