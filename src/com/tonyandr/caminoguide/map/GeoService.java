package com.tonyandr.caminoguide.map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tonyandr.caminoguide.constants.AppConstants;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Tony on 04-Feb-15.
 */
public class GeoService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, AppConstants {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    //GMS
    protected static final String TAG = "location-updates-sample";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 8000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // GMS
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    public Location mCurrentLocation;
    protected String mLastUpdateTime;

    static final String KEY_CURRENT_LOCATION = "mCurrentLocation";
    static final String KEY_LAST_UPD_TIME = "mLastUpdateTime";
    static final String KEY_SERVICE_ACTION = "GeoService";

    private SharedPreferences mPrefs;

//
//    public GeoService() {
//        super("GeoService");
//    }

//    @Override
//    protected void onHandleIntent(Intent workIntent) {
//
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Intent intent = new Intent();
            intent.setAction("GeoService");
            intent.putExtra(KEY_CURRENT_LOCATION, mCurrentLocation);
            sendBroadcast(intent);

            saveToPrefs();
        }
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        saveToPrefs();

        Intent intent = new Intent();
        intent.setAction(KEY_SERVICE_ACTION);
        intent.putExtra(KEY_CURRENT_LOCATION, mCurrentLocation);
        intent.putExtra(KEY_LAST_UPD_TIME, mLastUpdateTime);
        sendBroadcast(intent);
    }

    private void saveToPrefs() {
        final SharedPreferences.Editor edit = mPrefs.edit();
        if (mCurrentLocation != null) {
            edit.putFloat(LOCATION_KEY_LAT, (float) mCurrentLocation.getLatitude());
            edit.putFloat(LOCATION_KEY_LNG, (float) mCurrentLocation.getLongitude());
        }
        if (mLastUpdateTime != null) {
            edit.putString(KEY_LAST_UPD_TIME, mLastUpdateTime);
        }
        edit.commit();
    }

}
