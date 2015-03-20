// Created by plusminus on 00:23:14 - 03.10.2008
package com.tonyandr.caminoguide.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tonyandr.caminoguide.NavigationDrawerLayout;
import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 */
public class MapActivity extends ActionBarActivity implements AppConstants,SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mPrefs;
    SharedPreferences settings;
    private Boolean doubleBackToExitPressedOnce = false;
    private FragmentManager fm;
    private String mFullAppPath = Environment.getExternalStorageDirectory().getPath() + APP_PATH;

    private Toolbar toolbar;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(this);//get the preferences that are allowed to be given
        //set the listener to listen for changes in the preferences
        settings.registerOnSharedPreferenceChangeListener(this);
        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.setContentView(R.layout.activity_map);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationDrawerLayout drawerFragment = (NavigationDrawerLayout) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_drawer);
        drawerFragment.setUp(R.id.fragment_nav_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        fm = this.getFragmentManager();


//
//        if (settings.getString("pref_key_choose_map", "1").equals("3")) {
//            if (fm.findFragmentByTag(OSM_FRAGMENT_TAG) == null) {
//                OSMFragment mapFragment = OSMFragment.newInstance();
//                fm.beginTransaction().add(R.id.map_container, mapFragment, OSM_FRAGMENT_TAG).commit();
//            }
//        } else if (settings.getString("pref_key_choose_map", "1").equals("1")){
//            if (fm.findFragmentByTag(GMS_FRAGMENT_TAG) == null) {
//                GMapFragment mapFragment = GMapFragment.newInstance();
//                fm.beginTransaction().add(R.id.map_container, mapFragment, GMS_FRAGMENT_TAG).commit();
//            }
//        } else {
//            if (fm.findFragmentByTag(MF_FRAGMENT_TAG) == null) {
//                MapsForgeFragment mapFragment = MapsForgeFragment.newInstance();
//                fm.beginTransaction().add(R.id.map_container, mapFragment, MF_FRAGMENT_TAG).commit();
//            }
//        }

        checkGPS();
    }

    private void checkGPS() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
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
            message = "Your current location:\n\nNo data available :(";
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
            if (fm.findFragmentByTag(GMS_FRAGMENT_TAG) != null) {
                fm.beginTransaction().remove(fm.findFragmentByTag(GMS_FRAGMENT_TAG)).commit();
            }
            MapsForgeFragment mapFragment = MapsForgeFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mapFragment, MF_FRAGMENT_TAG).commit();
        }
    }
    private void startGoogleMapsFragment() {
        if (fm.findFragmentByTag(GMS_FRAGMENT_TAG) == null) {
            if (fm.findFragmentByTag(MF_FRAGMENT_TAG) != null) {
                fm.beginTransaction().remove(fm.findFragmentByTag(MF_FRAGMENT_TAG)).commit();
            }
            GMapFragment mapFragment = GMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mapFragment, GMS_FRAGMENT_TAG).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fm = this.getFragmentManager();
        File map_file = new File(mFullAppPath + APP_OFFLINE_MAP_FILE);

        if (settings.getString("pref_key_choose_map", "1").equals(MF_FRAGMENT_TAG)) {
            if (!map_file.exists()) {
                new CopyAssetsTask().execute();
            } else {
                startMapsForgeFragment();
            }
        } else {
            startGoogleMapsFragment();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settings.unregisterOnSharedPreferenceChangeListener(this);
        stopService(new Intent(this, GeoService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, GeoService.class));
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private boolean copyAssets() {
        boolean success = true;
        AssetManager assetManager = getAssets();
        String[] files = null;
        String output = Environment.getExternalStorageDirectory().getPath() + APP_PATH;
        String folder = "CaminoGuideOffline";
        try {
            files = assetManager.list(folder);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
            success = false;
        }
        File outDir = new File(output);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(folder +"/" +filename);
                File outFile = new File(output, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
                success = false;
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
        return success;
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }


    class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

        public  CopyAssetsTask() {

        }
        private boolean failed = false;

        @Override
        protected void onPreExecute() {
            Toast.makeText(MapActivity.this, "Preparing map, please wait...", Toast.LENGTH_LONG).show();
            showLoadingBanner();
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (copyAssets()) {
                File zip = new File(mFullAppPath + "CaminoGuideOffline.zip");
                try {
                    unzip(zip,new File(mFullAppPath));
                } catch (IOException e) {
                    e.printStackTrace();
                    failed = true;
                }
                if (zip.exists()) {
                    zip.delete();
                }
            } else {
                failed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideLoadingBanner();
            if (!failed) {
                startMapsForgeFragment();
            } else {
                startGoogleMapsFragment();
                Toast.makeText(MapActivity.this,"Error while unzipping map file!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showLoadingBanner() {
        ((TextView) ((ViewGroup)findViewById(R.id.progress_drawing_id)).findViewById(R.id.progress_drawing_text)).setText("Preparing map, please wait...");
        (findViewById(R.id.progress_drawing_id)).setVisibility(View.VISIBLE);
    }

    private void hideLoadingBanner() {
        (findViewById(R.id.progress_drawing_id)).setVisibility(View.GONE);
    }
}
