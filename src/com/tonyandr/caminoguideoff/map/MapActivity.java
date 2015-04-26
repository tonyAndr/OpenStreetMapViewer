// Created by plusminus on 00:23:14 - 03.10.2008
package com.tonyandr.caminoguideoff.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.tonyandr.caminoguideoff.NavigationDrawerLayout;
import com.tonyandr.caminoguideoff.R;
import com.tonyandr.caminoguideoff.constants.AppConstants;

import java.text.DateFormat;
import java.util.Date;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 */
public class MapActivity extends ActionBarActivity implements AppConstants {

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoKsruwYkVNt2YQHOx7fLKSDLjHwr7wN5Yle5xAQ2CVFB7dyAqP/emryFEq9fcsbzDVXmh0iSZtBCQn5aRRGJfsSO+QHmMgpGaRSvXmfdBVslxKC8SmHyqy3LCHJOW19of6sPvEuBM0FySac2LYzurjplKaLP9Ebv9H8hjPy3WdJ3KDV/ywT2w4f9hf14q+a8SZrLkaD9r4ky/P0j9ixOgoyFEWeS6f9UCvuqaL0Xk+8CqONRTEVRk0y5NrO4S6stkEmw1K2YvrLZ1nfKNFDpRrV8R0wZ423umr6BiiPb47SGciGk4NjxXm7w1cuaA35JIEgm0nWNbW3kRYw3IXq/AwIDAQAB";

    // Generate your own 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[]{
            -21, 31, 41, -22, -32, -42, 122, -62, 63, 36, -26, -16, 11, -111, -22, -33, -11, 99, -99, 9
    };  //

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    // A handler on the UI thread.
    private Handler mHandler;
    private boolean mAutoRetry = true;

    private SharedPreferences mPrefs;
    SharedPreferences settings;
    private Boolean doubleBackToExitPressedOnce = false;
    private FragmentManager fm;
    private String mFullAppPath = Environment.getExternalStorageDirectory().getPath() + APP_PATH;
    private Toolbar toolbar;

    // ===========================================================
    // Constructors
    // ===========================================================
    //**

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_map);

//        mStatusText = (TextView) findViewById(R.id.status_text);
//        mCheckLicenseButton = (Button) findViewById(R.id.check_license_button);
//        mCheckLicenseButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                doCheck();
//            }
//        });
        mHandler = new Handler();
        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
                BASE64_PUBLIC_KEY);

        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!mPrefs.contains(KEY_LICENSE)) {
            if (isNetworkAvailable()) {
                doCheck();
            }
        }


        settings = PreferenceManager.getDefaultSharedPreferences(this);//get the preferences that are allowed to be given
        //set the listener to listen for changes in the preferences
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationDrawerLayout drawerFragment = (NavigationDrawerLayout) getFragmentManager().findFragmentById(R.id.fragment_nav_drawer);
        drawerFragment.setUp(R.id.fragment_nav_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        fm = this.getFragmentManager();

        checkGPS();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected Dialog onCreateDialog(int id) {
        final boolean bRetry = id == 1;
        return new AlertDialog.Builder(this)
                .setTitle(R.string.unlicensed_dialog_title)
                .setMessage(bRetry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body)
                .setPositiveButton(bRetry ? R.string.retry_button : R.string.buy_button, new DialogInterface.OnClickListener() {
                    boolean mRetry = bRetry;

                    public void onClick(DialogInterface dialog, int which) {
                        if (mRetry) {
                            doCheck();
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "http://market.android.com/details?id=" + getPackageName()));
                            startActivity(marketIntent);
                        }
                    }
                })
                .setNegativeButton(R.string.quit_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
    }

    private void doCheck() {
//                mCheckLicenseButton.setEnabled(false);
                setProgressBarIndeterminateVisibility(true);
                mChecker.checkAccess(mLicenseCheckerCallback);
    }

    private void displayResult(final String result) {
        mHandler.post(new Runnable() {
            public void run() {
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    private void displayDialog(final boolean showRetry) {
        mHandler.post(new Runnable() {
            public void run() {
                setProgressBarIndeterminateVisibility(false);
                showDialog(showRetry ? 1 : 0);
            }
        });
    }


    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Should allow user access.
            Log.e("lic", "allow");
            displayResult(getString(R.string.allow));
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(KEY_LICENSE,"allow");
            editor.commit();
        }

        public void dontAllow(int policyReason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            displayResult(getString(R.string.dont_allow));
            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to Market.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.
            if (policyReason == Policy.RETRY) {
                if (mAutoRetry) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doCheck();
                            mAutoRetry = false;
                            Log.e("lic", "autoretry");
                        }
                    }, 1200);
                } else {
                    displayDialog(true);
                }
            } else {
                displayDialog(false);
            }

        }

        public void applicationError(int errorCode) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
            String result = String.format(getString(R.string.application_error), errorCode);
            displayResult(result);
        }
    }

    private void checkGPS() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Where are you?");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void infoDialog() {
        String message = "";
        if (mPrefs.contains("location-string")) {
            String[] loc_string = mPrefs.getString("location-string", "").split(",");
            if (loc_string.length > 1) {
                message = "Your current location:\n\n" +
                        "Latitude: " + Double.parseDouble(loc_string[0]) + "\n" +
                        "Longitude: " + Double.parseDouble(loc_string[1]) + "\n\n" +
                        "Last update: " + DateFormat.getTimeInstance().format(new Date(Long.parseLong(loc_string[2])));
            }
        } else {
            message = "Your current location:\n\nCannot find you, sorry :(\n\nHave you enabled GPS?";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setMessage(message);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_map, menu);
//
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.info_my_location) {
//            infoDialog();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    private void startMapsForgeFragment() {
        if (fm.findFragmentByTag(MF_FRAGMENT_TAG) == null) {
//            if (fm.findFragmentByTag(GMS_FRAGMENT_TAG) != null) {
//                fm.beginTransaction().remove(fm.findFragmentByTag(GMS_FRAGMENT_TAG)).commit();
//            }
            MapsForgeFragment mapFragment = MapsForgeFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mapFragment, MF_FRAGMENT_TAG).commit();
        }
    }

    private void startGoogleMapsFragment() {
        if (fm.findFragmentByTag(GMS_FRAGMENT_TAG) == null) {
//            if (fm.findFragmentByTag(MF_FRAGMENT_TAG) != null) {
//                fm.beginTransaction().remove(fm.findFragmentByTag(MF_FRAGMENT_TAG)).commit();
//            }
            GMapFragment mapFragment = GMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mapFragment, GMS_FRAGMENT_TAG).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fm = this.getFragmentManager();

        if (settings.getString("pref_key_choose_map", "1").equals(MF_FRAGMENT_TAG)) {
            startMapsForgeFragment();
        } else {
            startGoogleMapsFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChecker.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

            if (fm.getBackStackEntryCount() == 0) {
                if (doubleBackToExitPressedOnce) {

                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                fm.popBackStack();
            }


    }
}
