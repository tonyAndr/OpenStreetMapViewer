package com.tonyandr.caminoguide.map;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.stages.StageActivity;
import com.tonyandr.caminoguide.utils.GMapReturnObject;
import com.tonyandr.caminoguide.utils.GeoMethods;
import com.tonyandr.caminoguide.utils.MarkerDataObject;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class GMapFragment extends MapFragment implements AppConstants {


    public GMapFragment() {
        // Required empty public constructor
    }

    private GoogleMap map;
    private BroadcastReceiver br;
    public Location mCurrentLocation;
    public Location mFinishLocation;
    public String mLastUpdateTime;
    public Boolean mFollowUserLocation;
    private Intent mServiceIntent;
    private GeoMethods geoMethods;
    private DrawingMethods drawingMethods;
    private Bundle bundle;
    private boolean mFirstCameraMove;
    SharedPreferences settings;
    private Boolean mDrawMarkers; // to not draw when recieved broadcast
    private Polyline line;

    private TextView mKmTogo;

    // Tasks
    private CalculateDistanceTask calculateDistanceTask;
    private DrawAllRouteTask drawAllRouteTask;
    private DrawAlbMarkersTask drawAlbMarkersTask;
    private DrawCityMarkersTask drawCityMarkersTask;

    private SharedPreferences mPrefs;

    private ArrayList<Marker> markers = new ArrayList<>();

    public static GMapFragment newInstance() {
        GMapFragment fragment = new GMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFollowUserLocation = false;
        mLastUpdateTime = "";
        mDrawMarkers = true;
        geoMethods = new GeoMethods(getActivity());
        drawingMethods = new DrawingMethods(getActivity());
        mKmTogo = ((TextView)getActivity().findViewById(R.id.km_togo_id));


//        mCurrentLocation = new Location("");
        mFirstCameraMove = false;
        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mPrefs.contains("location-string")) {
            String[] loc_string = mPrefs.getString("location-string", "").split(",");
            if (loc_string.length > 1) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(Double.parseDouble(loc_string[0]));
                mCurrentLocation.setLongitude(Double.parseDouble(loc_string[1]));
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(Long.parseLong(loc_string[2])));
            }
        }
        updateValuesFromBundle(savedInstanceState);

        map = getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        MapsInitializer.initialize(this.getActivity());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -2.9), MIN_ZOOM_LEVEL);
        map.animateCamera(cameraUpdate);

        ImageButton followMyLocation = (ImageButton) getActivity().findViewById(R.id.getMyLocBtn);
        ImageButton zoomInBtn = (ImageButton) getActivity().findViewById(R.id.zoomInBtn);
        ImageButton zoomOutBtn = (ImageButton) getActivity().findViewById(R.id.zoomOutBtn);
        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });


        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mLastUpdateTime = intent.getStringExtra(KEY_LAST_UPD_TIME);
                mCurrentLocation = intent.getParcelableExtra(KEY_CURRENT_LOCATION);
                if (mCurrentLocation != null) {
                    updateUI();

                    if (bundle != null && calculateDistanceTask != null && settings.getBoolean("pref_key_realtime_calculation", false)) {
                        if (calculateDistanceTask.getStatus() == AsyncTask.Status.FINISHED) {
                            calculateDistanceTask = new CalculateDistanceTask();
                            calculateDistanceTask.execute();
//                            Toast.makeText(getActivity(), "Re-calculation...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (mFollowUserLocation != false) {
                    followUser();
                }

            }
        };

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mCurrentLocation = location;
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(location.getTime()));
                updateUI();

                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("location-string", mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + "," + mCurrentLocation.getTime());
                editor.commit();
//                Log.w(DEBUGTAG, mCurrentLocation.toString());

                if (bundle != null && calculateDistanceTask != null && settings.getBoolean("pref_key_realtime_calculation", false)) {
                    if (calculateDistanceTask.getStatus() == AsyncTask.Status.FINISHED) {
                        calculateDistanceTask = new CalculateDistanceTask();
                        calculateDistanceTask.execute();
//                            Toast.makeText(getActivity(), "Re-calculation...", Toast.LENGTH_SHORT).show();
                    }
                }

                if (mFollowUserLocation != false) {
                    followUser();
                }
            }
        });
        followMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!mFollowUserLocation) {
//                    mFollowUserLocation = true;
//                    Toast toast = Toast.makeText(getActivity(), "GPS Tracking On", Toast.LENGTH_SHORT);
//                    toast.show();
//                } else {
//                    mFollowUserLocation = false;
//                    Toast toast = Toast.makeText(getActivity(), "GPS Tracking Off", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
                if (mCurrentLocation != null) {
                    LatLng point = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    CameraUpdate cameraUpdate;
                    if (map.getCameraPosition().zoom < TRACK_ZOOM_LEVEL) {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, TRACK_ZOOM_LEVEL);
                    } else {
                        cameraUpdate = CameraUpdateFactory.newLatLng(point);
                    }
                    map.animateCamera(cameraUpdate);
                }
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom < SHOW_MARKERS_ZOOM_LEVEL) {
                    for (Marker item : markers) {
                        item.setVisible(false);
                    }
                } else {
                    for (Marker item : markers) {
                        item.setVisible(true);
                    }
                }
            }
        });



//        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                showMapControls();
//            }
//        });

        IntentFilter intFilt = new IntentFilter(KEY_SERVICE_ACTION);
        getActivity().registerReceiver(br, intFilt);
    }

    private void updateUI() {
        String lon = "Long: " + String.valueOf(mCurrentLocation.getLongitude());
        String lat = " Lat: " + String.valueOf(mCurrentLocation.getLatitude());
        String tim = " Time: " + mLastUpdateTime;

        if (getActivity() instanceof MapActivity) {
            ((MapActivity) getActivity()).geoOutTextViewLon.setText(lon);
            ((MapActivity) getActivity()).geoOutTextViewLat.setText(lat);
            ((MapActivity) getActivity()).geoOutTextViewTime.setText(tim);
        }
        if (getActivity() instanceof StageActivity) {
            ((StageActivity) getActivity()).geoOutTextViewLon.setText(lon);
            ((StageActivity) getActivity()).geoOutTextViewLat.setText(lat);
            ((StageActivity) getActivity()).geoOutTextViewTime.setText(tim);
        }
        if (getActivity() instanceof MapActivity) {
            if (!mFirstCameraMove) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), FIRST_SHOW_ZOOM_LEVEL));
                mFirstCameraMove = true;
            }
        }

    }

    private void followUser() {
        LatLng point = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        CameraUpdate cameraUpdate;
        if (map.getCameraPosition().zoom < TRACK_ZOOM_LEVEL) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, TRACK_ZOOM_LEVEL);
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(point);
        }
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onResume() {
        super.onResume();

        showMapControls();

        if (getActivity() instanceof StageActivity) {
            if (settings.getBoolean("pref_key_info_geo", false)) {
                ((StageActivity) getActivity()).geoOutTextViewLon.setVisibility(View.VISIBLE);
                ((StageActivity) getActivity()).geoOutTextViewLat.setVisibility(View.VISIBLE);
                ((StageActivity) getActivity()).geoOutTextViewTime.setVisibility(View.VISIBLE);
//                ((TextView)getActivity().findViewById(R.id.km_togo_id)).setVisibility(View.VISIBLE);
            } else {
                ((StageActivity) getActivity()).geoOutTextViewLon.setVisibility(View.GONE);
                ((StageActivity) getActivity()).geoOutTextViewLat.setVisibility(View.GONE);
                ((StageActivity) getActivity()).geoOutTextViewTime.setVisibility(View.GONE);
            }
        }
        if (getActivity() instanceof MapActivity) {
            showMapControls();
        }
        if (mFollowUserLocation) {
            Toast.makeText(getActivity(), "GPS Tracking On", Toast.LENGTH_SHORT).show();
        }

//        mServiceIntent = new Intent(getActivity(), GeoService.class);
//        getActivity().startService(mServiceIntent);

        try {
            drawLogic();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showMapControls() {
        getActivity().findViewById(R.id.zoomInBtn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.zoomOutBtn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.getMyLocBtn).setVisibility(View.VISIBLE);
    }
    private void hideMapControls() {
        getActivity().findViewById(R.id.zoomInBtn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.zoomOutBtn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.getMyLocBtn).setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideMapControls();

        if (getActivity() instanceof StageActivity) {
            ((StageActivity) getActivity()).geoOutTextViewLon.setVisibility(View.GONE);
            ((StageActivity) getActivity()).geoOutTextViewLat.setVisibility(View.GONE);
            ((StageActivity) getActivity()).geoOutTextViewTime.setVisibility(View.GONE);
            mKmTogo.setVisibility(View.GONE);

        }

        final SharedPreferences.Editor edit = mPrefs.edit();
        if (mCurrentLocation != null) {
            edit.putString("location-string", mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + "," + mCurrentLocation.getTime());
        }
        edit.commit();

        finishAllProcesses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishAllProcesses();
    }

    private void finishAllProcesses() {
        if (br != null) {
            getActivity().unregisterReceiver(br);
            br = null;
        }
        if (mServiceIntent != null) {
            getActivity().stopService(mServiceIntent);
            mServiceIntent = null;
        }
        if (calculateDistanceTask != null) {
            if (calculateDistanceTask.getStatus() != AsyncTask.Status.FINISHED) {
                calculateDistanceTask.cancel(true);
            }
        }
        if (drawAllRouteTask != null) {
            if (drawAllRouteTask.getStatus() != AsyncTask.Status.FINISHED) {
                drawAllRouteTask.cancel(true);
            }
        }
        if ( drawAlbMarkersTask != null) {
            if (drawAlbMarkersTask.getStatus() != AsyncTask.Status.FINISHED) {
                drawAlbMarkersTask.cancel(true);
            }
        }
        if (drawCityMarkersTask != null) {
            if (drawCityMarkersTask.getStatus() != AsyncTask.Status.FINISHED) {
                drawCityMarkersTask.cancel(true);
            }
        }

        hideLoadingBanner();
    }
    // GMS Block
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            if (savedInstanceState.keySet().contains(KEY_FOLLOW_USER)) {
                mFollowUserLocation = savedInstanceState.getBoolean(KEY_FOLLOW_USER);
            }
            if (savedInstanceState.keySet().contains(KEY_FIRST_CAMERA_MOVE)) {
                mFirstCameraMove = savedInstanceState.getBoolean(KEY_FIRST_CAMERA_MOVE);
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        savedInstanceState.putBoolean(KEY_FOLLOW_USER, mFollowUserLocation);
        savedInstanceState.putBoolean(KEY_FIRST_CAMERA_MOVE, mFirstCameraMove);

        super.onSaveInstanceState(savedInstanceState);
    }


    private class CalculateDistanceTask extends AsyncTask<Void, Void, Void> {
        public CalculateDistanceTask() {

        }

        private GMapReturnObject gMapReturnObject;
        private double distanceToFinish;

        @Override
        protected void onPreExecute() {
            if (mDrawMarkers)
                showLoadingBanner("Drawing route...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                gMapReturnObject = drawingMethods.drawDistanceRouteGMAP(mCurrentLocation, mFinishLocation);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            distanceToFinish = gMapReturnObject.length;
            if (line != null) {
                line.remove();
            }
            line = map.addPolyline(gMapReturnObject.polylineOptions);

            if (mDrawMarkers) {
                drawCityMarkersTask = new DrawCityMarkersTask();
                drawCityMarkersTask.execute();
            }
            mKmTogo.setVisibility(View.VISIBLE);
            mKmTogo.setAlpha(0.0f);
            mKmTogo.setText(String.format("%.1f", distanceToFinish) + " KM LEFT");
            mKmTogo.animate()
                    .setDuration(500)
                    .alpha(1.0f);
            hideLoadingBanner();
        }
    }

    class DrawAllRouteTask extends AsyncTask<Integer, Void, Void> {
        public DrawAllRouteTask() {

        }

        private ArrayList<PolylineOptions> polylineOptions;

        @Override
        protected void onPreExecute() {
            showLoadingBanner("Drawing route...");
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                polylineOptions = drawingMethods.drawAllRouteGMAP(params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (PolylineOptions item:polylineOptions) {
                Polyline line = map.addPolyline(item);
            }

            if (mDrawMarkers) {
                drawCityMarkersTask = new DrawCityMarkersTask();
                drawCityMarkersTask.execute();
            }

            hideLoadingBanner();
        }
    }

    class DrawAlbMarkersTask extends AsyncTask<Void, Void, Void> {
        public DrawAlbMarkersTask() {

        }

        private ArrayList<MarkerDataObject> markersData;

        @Override
        protected void onPreExecute() {
            showLoadingBanner("Drawing markers...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                markersData = drawingMethods.drawAlbMarkersGMAP(mFinishLocation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            (getActivity().findViewById(R.id.progress_drawing_id)).setVisibility(View.GONE);

            for (MarkerDataObject item : markersData) {

                markers.add(map.addMarker(getMarkerOptions(item)));
            }
            if (map.getCameraPosition().zoom < SHOW_MARKERS_ZOOM_LEVEL) {
                for (Marker item : markers) {
                    item.setVisible(false);
                }
            } else {
                for (Marker item : markers) {
                    item.setVisible(true);
                }
            }
            hideLoadingBanner();
            mDrawMarkers = false;
        }
    }

    private MarkerOptions getMarkerOptions(MarkerDataObject object) {
        return new MarkerOptions()
                .position(object.pointLocation)
                .title(object.title)
                .snippet(object.snippet)
                .icon(BitmapDescriptorFactory.fromResource(object.iconId));
    }

    class DrawCityMarkersTask extends AsyncTask<Void, Void, Void> {
        public DrawCityMarkersTask() {

        }

        private ArrayList<MarkerDataObject> markersData;

        @Override
        protected void onPreExecute() {
            showLoadingBanner("Drawing markers...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                markersData = drawingMethods.drawCityMarkersGMAP();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (MarkerDataObject item : markersData) {
                map.addMarker(getMarkerOptions(item));
            }
            drawAlbMarkersTask = new DrawAlbMarkersTask();
            drawAlbMarkersTask.execute();
            hideLoadingBanner();
        }
    }

    private void drawLogic() throws JSONException {

        bundle = getArguments();
        if (bundle != null) {
            mFinishLocation = new Location("");
            mFinishLocation.setLatitude(bundle.getDouble("lat"));
            mFinishLocation.setLongitude(bundle.getDouble("lng"));
            getActivity().setTitle(bundle.getString("title"));
            if (mCurrentLocation != null) {
                if (areaLimitSpain.contains(new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))) {
                    calculateDistanceTask = new CalculateDistanceTask();
                    calculateDistanceTask.execute();
                    if (bundle.getBoolean("globe", false) && bundle.getBoolean("near", false)) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), SHOW_STAGE_ZOOM_LEVEL));
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()), TRACK_ZOOM_LEVEL));
                    }
                } else {
                    drawAllRouteTask = new DrawAllRouteTask();
                    drawAllRouteTask.execute(bundle.getInt("stage_id"));
                    if (bundle.getBoolean("globe", false) && bundle.getBoolean("near", false)) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()), SHOW_STAGE_ZOOM_LEVEL));
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()), TRACK_ZOOM_LEVEL));
                    }
                }

            } else {
                drawAllRouteTask = new DrawAllRouteTask();
                drawAllRouteTask.execute(bundle.getInt("stage_id"));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()), TRACK_ZOOM_LEVEL));
            }
        } else {
            drawAllRouteTask = new DrawAllRouteTask();
            drawAllRouteTask.execute(0);
            if (mCurrentLocation != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), SHOW_STAGE_ZOOM_LEVEL));
            } else {
                LatLng startPoint = new LatLng(42.4167413, -2.7294623);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, MIN_ZOOM_LEVEL));
            }
        }
    }

    private void showLoadingBanner(String resource) {
        ((TextView)(getActivity().findViewById(R.id.progress_drawing_id)).findViewById(R.id.progress_drawing_text)).setText(resource);
        (getActivity().findViewById(R.id.progress_drawing_id)).setVisibility(View.VISIBLE);
    }
    private void hideLoadingBanner(){
        (getActivity().findViewById(R.id.progress_drawing_id)).setVisibility(View.GONE);
    }
}
