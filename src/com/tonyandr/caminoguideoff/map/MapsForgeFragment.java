package com.tonyandr.caminoguideoff.map;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.tonyandr.caminoguideoff.R;
import com.tonyandr.caminoguideoff.constants.AppConstants;
import com.tonyandr.caminoguideoff.stages.StageActivity;
import com.tonyandr.caminoguideoff.utils.GeoMethods;

import org.json.JSONException;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layer.MyLocationOverlay;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tony on 18-Mar-15.
 */
//import android.graphics.drawable.Drawable;

public class MapsForgeFragment extends Fragment implements AppConstants {

    // name of the map file in the external storage
    private static final String MAPFILE = APP_PATH+APP_OFFLINE_MAP_FILE;
    private static final String RENDER = Environment.getExternalStorageDirectory().getPath() + APP_PATH;

    public Location mCurrentLocation;
    public Location mFinishLocation;
    public String mLastUpdateTime;
    public Boolean mFollowUserLocation;
    private Bundle bundle;
    private SharedPreferences mPrefs;
    private SharedPreferences settings;
    private DrawingMethods drawingMethods;
    private GeoMethods geoMethods;
    private TextView mKmTogo;
    private ImageButton mZoomIn;
    private ImageButton mZoomOut;
    private ImageButton followMyLocation;

    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private MyLocationOverlay myLocationOverlay;

    // Tasks
    private CalculateDistanceTask calculateDistanceTask;
    private DrawAllRouteTask drawAllRouteTask;
    private DrawAlbMarkersTask drawAlbMarkersTask;
    private DrawCityMarkersTask drawCityMarkersTask;

    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler; // Update location to mCurrentLocation

    public static MapsForgeFragment newInstance() {
        MapsForgeFragment fragment = new MapsForgeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mPrefs.contains("location-string")) {
            String[] loc_string = mPrefs.getString("location-string", "").split(",");
            if (loc_string.length > 1) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(Double.parseDouble(loc_string[0]));
                mCurrentLocation.setLongitude(Double.parseDouble(loc_string[1]));
                mCurrentLocation.setTime(Long.parseLong(loc_string[2]));
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(Long.parseLong(loc_string[2])));
                Log.e("loc", "from prefs: " + mCurrentLocation);
            }
        }
//        updateValuesFromBundle(savedInstanceState);
        AndroidGraphicFactory.createInstance(getActivity().getApplication());

        this.mapView = new MapView(getActivity());

        this.mapView.setClickable(true);
        this.mapView.getMapScaleBar().setVisible(true);
        this.mapView.setBuiltInZoomControls(false);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 11);
        this.mapView.getModel().mapViewPosition.setZoomLevelMin((byte) 11);
        this.mapView.getModel().mapViewPosition.setZoomLevelMax((byte) 19);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 19);
        this.mapView.getModel().mapViewPosition.setMapLimit(new BoundingBox(42.193,-9.444, 43.428,-0.989));
        this.mapView.getMapZoomControls().setShowMapZoomControls(false);

        // create a tile cache of suitable size
        this.tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor());

        return mapView;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setHardwareAccelerationOff() {
        // Turn off hardware acceleration here, or in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    @Override
    public void onStart() {
        super.onStart();

        mHandler = new Handler();
        drawingMethods = new DrawingMethods(mapView,getActivity());
        geoMethods = new GeoMethods(getActivity());

        mKmTogo = ((TextView)getActivity().findViewById(R.id.km_togo_id));
        mZoomIn = (ImageButton) getActivity().findViewById(R.id.zoomInBtn);
        mZoomOut = (ImageButton) getActivity().findViewById(R.id.zoomOutBtn);
        followMyLocation = (ImageButton) getActivity().findViewById(R.id.getMyLocBtn);
        followMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentLocation = myLocationOverlay.getLastLocation();
                if (mCurrentLocation != null) {
                    mapView.getModel().mapViewPosition.animateTo(new LatLong(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
                    if (mapView.getModel().mapViewPosition.getZoomLevel() < TRACK_ZOOM_LEVEL) {
                        mapView.getModel().mapViewPosition.setZoomLevel((byte)TRACK_ZOOM_LEVEL);
                    }
                }
            }
        });

        setControlsAnimation();

        mZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getModel().mapViewPosition.zoomIn();
            }
        });
        mZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getModel().mapViewPosition.zoomOut();
            }
        });

        this.mapView.getModel().mapViewPosition.setCenter(new LatLong(42.42, -2.61));
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

        // tile renderer layer using internal render theme
//        MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
//        multiMapDataStore.addMapDataStore(new MapFile(getMapFile("/stage8.map")),true,true);
//        multiMapDataStore.addMapDataStore(new MapFile(getMapFile("/berlin.map")),true,true);
        this.tileRendererLayer = new TileRendererLayer(tileCache, new MapFile(getMapFile(MAPFILE)),
                this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        try {
            tileRendererLayer.setXmlRenderTheme(new ExternalRenderTheme(new File(RENDER + "andromaps_hike.xml")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        }

        Drawable drawable = getResources().getDrawable(R.drawable.ic_lmy_location);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);

        // create the overlay and tell it to follow the location
        this.myLocationOverlay = new MyLocationOverlay(getActivity(),
                this.mapView.getModel().mapViewPosition, bitmap);
        this.myLocationOverlay.setSnapToLocationEnabled(false);
//        this.myLocationOverlay.setSnapToLocationEnabled(true);

//        myLocationOverlay.onLocationChanged();
        // only once a layer is associated with a mapView the rendering starts
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
        this.mapView.getLayerManager().getLayers().add(this.myLocationOverlay);

//        AddRoadAxis(mapView.getLayerManager().getLayers());
        setHardwareAccelerationOff();
    }

    private void setControlsAnimation() {
        final float originX = mZoomIn.getScaleX();
        final float originY = mZoomIn.getScaleY();
        final float originXLoc = followMyLocation.getScaleX();
        final float originYLoc = followMyLocation.getScaleY();

        mZoomIn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(originX*1.2f).scaleY(originY * 1.2f).setDuration(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(originX).scaleY(originY).setDuration(100);
                        break;
                    default:break;

                }
                return false;
            }
        });
        mZoomOut.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(originX * 1.2f).scaleY(originY * 1.2f).setDuration(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(originX).scaleY(originY).setDuration(100);
                        break;
                    default:break;

                }
                return false;
            }
        });
        followMyLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(originXLoc * 1.2f).scaleY(originYLoc * 1.2f).setDuration(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(originXLoc).scaleY(originYLoc).setDuration(100);
                        break;
                    default:break;

                }
                return false;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mapView.getLayerManager().getLayers().remove(this.tileRendererLayer);
        this.tileRendererLayer.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.myLocationOverlay.disableMyLocation();
        final SharedPreferences.Editor edit = mPrefs.edit();
        mCurrentLocation = myLocationOverlay.getLastLocation();
        if (mCurrentLocation != null) {
            edit.putString("location-string", mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + "," + mCurrentLocation.getTime());
            Log.e("loc", "save on pause: " + mCurrentLocation);
        }
        edit.commit();

        if (getActivity() instanceof StageActivity) {
            getActivity().findViewById(R.id.zoomInBtn).setVisibility(View.GONE);
            getActivity().findViewById(R.id.zoomOutBtn).setVisibility(View.GONE);
            getActivity().findViewById(R.id.getMyLocBtn).setVisibility(View.GONE);
            mKmTogo.setVisibility(View.GONE);
        }
        (getActivity().findViewById(R.id.progress_drawing_id)).setVisibility(View.GONE);


        finishAllProcesses();
        stopRepeatingTask();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.myLocationOverlay.enableMyLocation(false);

        if (mPrefs.contains("location-string")) {
            String[] loc_string = mPrefs.getString("location-string", "").split(",");
            if (loc_string.length > 1) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(Double.parseDouble(loc_string[0]));
                mCurrentLocation.setLongitude(Double.parseDouble(loc_string[1]));
                mCurrentLocation.setTime(Long.parseLong(loc_string[2]));
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(Long.parseLong(loc_string[2])));
                Log.e("loc", "from prefs on resume: " + mCurrentLocation);
            }
        }

        bundle = getArguments();
        if ((bundle != null && getActivity() instanceof StageActivity) || getActivity() instanceof MapActivity) {
            getActivity().findViewById(R.id.zoomInBtn).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.zoomOutBtn).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.getMyLocBtn).setVisibility(View.VISIBLE);
        }

        try {
            drawLogic();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        startRepeatingTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.tileCache.destroy();
        this.mapView.getModel().mapViewPosition.destroy();
        this.mapView.destroy();
        AndroidGraphicFactory.clearResourceMemoryCache();
    }

    private File getMapFile(String filename) {
        return new File(Environment.getExternalStorageDirectory().getPath() + filename);
    }


    private void drawLogic() throws JSONException {
        bundle = getArguments();
        if (bundle != null) {
            mFinishLocation = new Location("");
            mFinishLocation.setLatitude(bundle.getDouble("lat"));
            mFinishLocation.setLongitude(bundle.getDouble("lng"));
            getActivity().setTitle(bundle.getString("title"));
            if (mCurrentLocation != null) {
                Log.e("loc", "drawLogic: " + mCurrentLocation);
                if (areaLimitSpainGMap.contains(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))) {
                    calculateDistanceTask = new CalculateDistanceTask();
                    calculateDistanceTask.execute();
                    if (bundle.getBoolean("globe", false) && bundle.getBoolean("near", false)) {
                        mapView.getModel().mapViewPosition.setCenter(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        mapView.getModel().mapViewPosition.setZoomLevel((byte) SHOW_STAGE_ZOOM_LEVEL);
                    } else {
                        mapView.getModel().mapViewPosition.setCenter(new LatLong(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()));
                        mapView.getModel().mapViewPosition.setZoomLevel((byte) TRACK_ZOOM_LEVEL);
                    }
                } else {
                    drawAllRouteTask = new DrawAllRouteTask();
                    drawAllRouteTask.execute(bundle.getInt("stage_id"));
                    if (bundle.getBoolean("globe", false)) {
                        cameraToBBox(bundle.getInt("stage_id"));
//                        mapView.getModel().mapViewPosition.setCenter(new LatLong(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()));
//                        mapView.getModel().mapViewPosition.setZoomLevel((byte) SHOW_STAGE_ZOOM_LEVEL);
                    } else {
                        mapView.getModel().mapViewPosition.setCenter(new LatLong(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()));
                        mapView.getModel().mapViewPosition.setZoomLevel((byte) TRACK_ZOOM_LEVEL);
                    }
                }
            } else {
                drawAllRouteTask = new DrawAllRouteTask();
                drawAllRouteTask.execute(bundle.getInt("stage_id"));
                if (bundle.getBoolean("globe", false)) {
                    cameraToBBox(bundle.getInt("stage_id"));
//                        mapView.getModel().mapViewPosition.setCenter(new LatLong(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()));
//                        mapView.getModel().mapViewPosition.setZoomLevel((byte) SHOW_STAGE_ZOOM_LEVEL);
                } else {
                    mapView.getModel().mapViewPosition.setCenter(new LatLong(mFinishLocation.getLatitude(), mFinishLocation.getLongitude()));
                    mapView.getModel().mapViewPosition.setZoomLevel((byte) TRACK_ZOOM_LEVEL);
                }
            }

        } else {
            drawAllRouteTask = new DrawAllRouteTask();
            drawAllRouteTask.execute(0);
//            testKmlDraw();
            if (mCurrentLocation != null) {
                if (areaLimitSpainGMap.contains(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))) {
                    mapView.getModel().mapViewPosition.setCenter(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    mapView.getModel().mapViewPosition.setZoomLevel((byte) SHOW_STAGE_ZOOM_LEVEL);
                } else {
                    LatLong startPoint = new LatLong(42.4167413, -2.7294623);
                    mapView.getModel().mapViewPosition.setCenter(startPoint);
                    mapView.getModel().mapViewPosition.setZoomLevel((byte) SHOW_STAGE_ZOOM_LEVEL);
                }
            } else {
                LatLong startPoint = new LatLong(42.4167413, -2.7294623);
                mapView.getModel().mapViewPosition.setCenter(startPoint);
                mapView.getModel().mapViewPosition.setZoomLevel((byte) SHOW_STAGE_ZOOM_LEVEL);
            }
        }
    }

    private void cameraToBBox(int stageid) throws JSONException {
        BoundingBox bbox = geoMethods.getStageMFBBox(stageid);
//        Dimension dimension = mapView.getModel().mapViewDimension.getDimension();
        mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                bbox.getCenterPoint(),
                (byte) MIN_ZOOM_LEVEL));
    }


    private class CalculateDistanceTask extends AsyncTask<Void, Void, Void> {
        public CalculateDistanceTask() {
        }

        private double distanceToFinish;

        @Override
        protected void onPreExecute() {
            showLoadingBanner(getString(R.string.progress_drawing_route));
            Log.e("loc", "onPreEx: " + mCurrentLocation);
            if (myLocationOverlay != null && mCurrentLocation == null) {
                mCurrentLocation = myLocationOverlay.getLastLocation();
                Log.e("loc", "in task: " + mCurrentLocation);
            }
        }


        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.e("loc", "doinbg: " + mCurrentLocation);
                distanceToFinish = drawingMethods.drawDistanceRouteMF(mapView.getLayerManager().getLayers(), mCurrentLocation, mFinishLocation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            drawAlbMarkersTask = new DrawAlbMarkersTask();
            drawAlbMarkersTask.execute();
            mapView.getLayerManager().redrawLayers();

// Start the animation

            mKmTogo.setVisibility(View.VISIBLE);
            mKmTogo.setAlpha(0.0f);
            mKmTogo.setText(String.format("%.1f", distanceToFinish) + " KM LEFT");
            mKmTogo.animate()
                    .setDuration(500)
                    .alpha(1.0f);
//            Toast.makeText(getActivity(), , Toast.LENGTH_LONG).show();
            hideLoadingBanner();
        }
    }

    class DrawAllRouteTask extends AsyncTask<Integer, Void, Void> {
        public DrawAllRouteTask() {

        }

        private ArrayList<org.mapsforge.map.layer.overlay.Polyline> list = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            showLoadingBanner(getString(R.string.progress_drawing_route));
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                list = drawingMethods.drawAllRouteMF(params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            stage_id = params[0];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (org.mapsforge.map.layer.overlay.Polyline item : list) {
                mapView.getLayerManager().getLayers().add(item);
            }
            drawAlbMarkersTask = new DrawAlbMarkersTask();
            drawAlbMarkersTask.execute();
            mapView.getLayerManager().redrawLayers();
            hideLoadingBanner();
        }
    }

    class DrawAlbMarkersTask extends AsyncTask<Void, Void, Void> {
        public DrawAlbMarkersTask() {

        }

        @Override
        protected void onPreExecute() {
            showLoadingBanner(getString(R.string.progress_drawing_markers));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                drawingMethods.drawAlbMarkersMF(mapView.getLayerManager().getLayers(), mFinishLocation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            drawCityMarkersTask = new DrawCityMarkersTask();
            drawCityMarkersTask.execute();
            mapView.getLayerManager().redrawLayers();
            hideLoadingBanner();
        }
    }

    class DrawCityMarkersTask extends AsyncTask<Void, Void, Void> {
        public DrawCityMarkersTask() {

        }

        @Override
        protected void onPreExecute() {
            showLoadingBanner(getString(R.string.progress_drawing_markers));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                drawingMethods.drawCityMarkersMF(mapView.getLayerManager().getLayers());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            mapView.getLayerManager().redrawLayers();
            hideLoadingBanner();
        }
    }

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
//            if (savedInstanceState.keySet().contains(KEY_FIRST_CAMERA_MOVE)) {
//                mFirstCameraMove = savedInstanceState.getBoolean(KEY_FIRST_CAMERA_MOVE);
//            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
//        savedInstanceState.putBoolean(KEY_FOLLOW_USER, mFollowUserLocation);
//        savedInstanceState.putBoolean(KEY_FIRST_CAMERA_MOVE, mFirstCameraMove);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void showLoadingBanner(String resource) {
        ((TextView) (getActivity().findViewById(R.id.progress_drawing_id)).findViewById(R.id.progress_drawing_text)).setText(resource);
        (getActivity().findViewById(R.id.progress_drawing_id)).setVisibility(View.VISIBLE);
    }

    private void hideLoadingBanner() {
        (getActivity().findViewById(R.id.progress_drawing_id)).setVisibility(View.GONE);
    }

    private void finishAllProcesses() {
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
        if (drawAlbMarkersTask != null) {
            if (drawAlbMarkersTask.getStatus() != AsyncTask.Status.FINISHED) {
                drawAlbMarkersTask.cancel(true);
            }
        }
        if (drawCityMarkersTask != null) {
            if (drawCityMarkersTask.getStatus() != AsyncTask.Status.FINISHED) {
                drawCityMarkersTask.cancel(true);
            }
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            if (mCurrentLocation == null) {
                if (myLocationOverlay != null) {
                    final SharedPreferences.Editor edit = mPrefs.edit();
                    mCurrentLocation = myLocationOverlay.getLastLocation();
                    if (mCurrentLocation != null) {
                        edit.putString("location-string", mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + "," + mCurrentLocation.getTime());
                    }
                    edit.commit();
                }
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void infoDialog() {
        String message = "";
        if (mCurrentLocation!=null) {
            message = "Your current location:\n\n" +
                    "Latitude: " + String.format("%.4f", mCurrentLocation.getLatitude()) + "\n" +
                    "Longitude: " + String.format("%.4f", mCurrentLocation.getLongitude()) + "\n\n" +
                    "Last update: " + DateFormat.getTimeInstance().format(new Date(mCurrentLocation.getTime()));
        } else {
            message = "Your current location:\n\nNo data available :(";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.info_my_location) {
            infoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
