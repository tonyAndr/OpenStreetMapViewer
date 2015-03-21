package com.tonyandr.caminoguide.stages;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.XLabels;
import com.google.android.gms.maps.model.LatLng;
import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.map.GMapFragment;
import com.tonyandr.caminoguide.map.MapsForgeFragment;
import com.tonyandr.caminoguide.utils.DBControllerAdapter;
import com.tonyandr.caminoguide.utils.GeoMethods;
import com.tonyandr.caminoguide.utils.JsonFilesHandler;
import com.tonyandr.caminoguide.utils.OnStageLocationData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStageView extends Fragment implements AppConstants {

    private int stageId;
    private String fromto;
    private SharedPreferences mPrefs;
    private SharedPreferences settings;
//    private GeoMethods geoMethods;

    private LineChart mChart;
    private TextView tvStagename;
    private TextView tvStagelength;
    private TextView tvKmDone;
    private TextView tvKmLeft;
    private JSONArray mJsonArr;
    private double mStageLength;
    private double mStageKmLeft;
    private double mStageKmDone;
    private ListView lvAlbergues;
    private ArrayList<StageViewAlbItem> mStageViewAlbItems;
    private StageViewAlberguesAdapter mAlberguesAdapter;
    private double finish_latitude;
    private double finish_longitude;
    private Boolean near = false;

    private TaskLoadData taskLoadData;
    private FillListViewTask fillListViewTask;
    private DBControllerAdapter dbController;


    public static final String KEY_STAGEID = "stageId";
    public static final String KEY_FROMTO = "fromto";
    public static final String DEF_FROMTO = "Saint Jean Pied de Port - Roncesvalles";
    public static final int DEF_STAGEID = 1;


    public FragmentStageView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

//        if (savedInstanceState != null) {
//            stageId = savedInstanceState.getInt(KEY_STAGEID, DEF_STAGEID);
//            fromto = savedInstanceState.getString(KEY_FROMTO, DEF_FROMTO);
//        }


        return inflater.inflate(R.layout.fragment_stage_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbController = DBControllerAdapter.getInstance(getActivity());

        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stageId = mPrefs.getInt(PREFS_STAGELIST_STAGEID, DEF_STAGEID);
        fromto = mPrefs.getString(PREFS_STAGELIST_FROMTO, DEF_FROMTO);

        mStageViewAlbItems = new ArrayList<>();
        mAlberguesAdapter = new StageViewAlberguesAdapter(getActivity(), mStageViewAlbItems);

        lvAlbergues = (ListView) getActivity().findViewById(R.id.lv_albergues);
        View header = getActivity().getLayoutInflater().inflate(R.layout.fragment_stage_view_header, null);
        lvAlbergues.addHeaderView(header, null, false);

        tvStagename = (TextView) getActivity().findViewById(R.id.tv_stagename);
//        tvStagelength = (TextView) getActivity().findViewById(R.id.tv_stagelength);
        tvKmDone = (TextView) getActivity().findViewById(R.id.tv_kmdone);
//        tvKmLeft = (TextView) getActivity().findViewById(R.id.tv_kmleft);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());//get the preferences that are allowed to be given

        lvAlbergues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StageViewAlbItem row = (StageViewAlbItem) parent.getItemAtPosition(position);

                Bundle bundle = new Bundle();
                bundle.putInt("stage_id", row.stage_id);
                bundle.putDouble("lat", row.lat);
                bundle.putDouble("lng", row.lng);
                bundle.putString("title", row.title);
if (settings.getString("pref_key_choose_map", "1") == GMS_FRAGMENT_TAG) {
                    GMapFragment gMapFragment = new GMapFragment();
                    gMapFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager.findFragmentByTag("FragmentView") != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.replace(R.id.stage_fragment_holder_id, gMapFragment, "GMapFragment");
                        transaction.addToBackStack(GMapFragment.class.getName());
                        transaction.commit();
                    }
                } else {
                    MapsForgeFragment mapsForgeFragment = new MapsForgeFragment();
                    mapsForgeFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager.findFragmentByTag("FragmentView") != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.replace(R.id.stage_fragment_holder_id, mapsForgeFragment, "MapsForgeFragment");
                        transaction.addToBackStack(MapsForgeFragment.class.getName());
                        transaction.commit();
                    }
                }


            }
        });

        ImageButton btnGlobe = (ImageButton) getActivity().findViewById(R.id.iv_globe);
        btnGlobe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title", fromto);
                bundle.putInt("stage_id", stageId);
                bundle.putDouble("lat", finish_latitude);
                bundle.putDouble("lng", finish_longitude);
                bundle.putBoolean("globe", true);
                bundle.putBoolean("near", near);

                    if (settings.getString("pref_key_choose_map", "1") == GMS_FRAGMENT_TAG) {
                    GMapFragment gMapFragment = new GMapFragment();
                    gMapFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager.findFragmentByTag("FragmentView") != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.replace(R.id.stage_fragment_holder_id, gMapFragment, "GMapFragment");
                        transaction.addToBackStack(GMapFragment.class.getName());
                        transaction.commit();
                    }
                } else {
                    MapsForgeFragment mapsForgeFragment = new MapsForgeFragment();
                    mapsForgeFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager.findFragmentByTag("FragmentView") != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.replace(R.id.stage_fragment_holder_id, mapsForgeFragment, "MapsForgeFragment");
                        transaction.addToBackStack(MapsForgeFragment.class.getName());
                        transaction.commit();
                    }
                }
            }
        });

        final float originX = btnGlobe.getScaleX();
        final float originY = btnGlobe.getScaleY();
        btnGlobe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(originX*1.2f).scaleY(originY*1.2f).setDuration(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(originX).scaleY(originY).setDuration(100);
                        break;
                    default:break;

                }
                return false;
            }
        });

        lvAlbergues.setAdapter(mAlberguesAdapter);
        chartSetup();
        taskLoadData = new TaskLoadData();
        taskLoadData.execute(stageId);

        fillListViewTask = new FillListViewTask();
        fillListViewTask.execute();

        tvStagename.setText(fromto);

        setHasOptionsMenu(true);
//        getJsonData(stageId);
    }

    class FillListViewTask extends AsyncTask<Void, StageViewAlbItem, Void> {
        public FillListViewTask() {

        }

        private List<StageViewAlbItem> data = new ArrayList<>();
        private JSONArray albJArr;

        @Override
        protected void onPreExecute() {
//            JsonFilesHandler jfh = new JsonFilesHandler(getActivity());
//            String alb_path = "json/albergues.json";
//            albJArr = jfh.parseJSONArr(alb_path);
            DBControllerAdapter dbControllerAdapter;
            dbControllerAdapter = DBControllerAdapter.getInstance(getActivity());
            try {
                albJArr = dbControllerAdapter.getAlbergues(stageId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String title, locality, tel, beds, type;
            int stage_id;
            double lat, lng;
            try {
                JSONObject prev_v = albJArr.getJSONObject(0);
                for (int i = 0; i < albJArr.length(); i++) {
                    JSONObject v = albJArr.getJSONObject(i);
                    if (stageId == v.getInt("stage")) {
                        title = "Albergue " + v.getString("title");
                        locality = v.getString("locality");
                        tel = v.getString("tel");
                        type = v.getString("type");
                        switch(type) {
                            case "P": type = "Private"; break;
                            case "Par": type = "Parroquial"; break;
                            case "X": type = "Private"; break;
                            case "M": type = "Municipal"; break;
                            default: break;
                        }
                        beds = v.getString("beds");
                        stage_id = v.getInt("stage");
                        lat = v.getDouble("lat");
                        lng = v.getDouble("lng");
                        boolean showHeader = false;
                        if (!locality.equals(prev_v.getString("locality")) || i == 0) {
                            showHeader = true;
                        } else {
                            showHeader = false;
                        }
                        publishProgress(new StageViewAlbItem(title, tel, type, beds, locality, showHeader, lat, lng, stage_id));

                        prev_v = v;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(StageViewAlbItem... values) {
            mAlberguesAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }


    private void chartSetup() {
        mChart = (LineChart) getActivity().findViewById(R.id.chart);
        mChart.setStartAtZero(false);
        // disable the drawing of values into the chart
        mChart.setDrawYValues(false);
        mChart.setDrawBorder(false);
        mChart.setDrawLegend(false);
        // no description text
        mChart.setDescription("Elevation");
        // enable value highlighting
        mChart.setHighlightEnabled(true);
        // enable touch gestures
        mChart.setTouchEnabled(false);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        mChart.setDrawGridBackground(false);
        mChart.setDrawVerticalGrid(true);
        mChart.animateY(2000);
        XLabels xl = mChart.getXLabels();
        xl.setPosition(XLabels.XLabelPosition.BOTTOM); // set the position
        mChart.invalidate();
    }


    private void addDataSet(ArrayList<String> xVals, ArrayList<Entry> yVals) {

        LineDataSet set = new LineDataSet(yVals, "DataSet");
        LineData lineData = new LineData(xVals, set);

        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setDrawFilled(true);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setCircleSize(5f);
        set.setColor(Color.rgb(244,68,68));
        set.setHighLightColor(Color.rgb(0, 150, 136));

        mChart.setData(lineData);

        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(KEY_KM_DONE, (float) mStageKmDone);
        editor.commit();
        if (taskLoadData != null) {
            if (taskLoadData.getStatus() != AsyncTask.Status.FINISHED)
                taskLoadData.cancel(true);
        }

        if (fillListViewTask != null)
            if (fillListViewTask.getStatus() != AsyncTask.Status.FINISHED)
                fillListViewTask.cancel(true);

        dbController.closeConnection();
    }

    @Override
    public void onResume() {
        super.onResume();
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        stageId = mPrefs.getInt(PREFS_STAGELIST_STAGEID, DEF_STAGEID);
        fromto = mPrefs.getString(PREFS_STAGELIST_FROMTO, DEF_FROMTO);
        if (mPrefs.contains(KEY_KM_DONE)) {
            mStageKmDone = mPrefs.getFloat(KEY_KM_DONE, 0);
        }
        getActivity().setTitle(fromto);

    }

    //    Getting last known position
    private Location getGPS() { // Position exists, if it was recieved on map activity (automatically on activity created)
        Location gps;
        if (mPrefs != null) {
            String[] loc_string = mPrefs.getString("location-string", "").split(",");
            if (loc_string.length > 1) {
                gps = new Location("");
                gps.setLatitude(Double.parseDouble(loc_string[0]));
                gps.setLongitude(Double.parseDouble(loc_string[1]));
                gps.setTime(Long.parseLong(loc_string[2]));
                return gps;
            }
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(KEY_KM_DONE, mStageKmDone);
    }


    //
    class TaskLoadData extends AsyncTask<Integer, Void, Void> {
        public TaskLoadData() {

        }

        private JsonFilesHandler jfh;
        private GeoMethods geoMethods;
        private ArrayList<String> xVals;
        private ArrayList<Entry> yVals;
        private String stageName;
        private int highlightId;

        public JSONArray jsonArr = new JSONArray();

        @Override
        protected void onPreExecute() {
//            adapter = (StagesAdapter) listView.getAdapter();
            jfh = new JsonFilesHandler(getActivity());
            geoMethods = new GeoMethods(getActivity());
            yVals = new ArrayList<>();
            xVals = new ArrayList<>();
        }


        @Override
        protected Void doInBackground(Integer... params) {
            JSONObject fileObj = jfh.parseJSONObj("json/stage" + params[0] + ".json");
            try {
                Location gps = getGPS();
                OnStageLocationData onStageLocationData = geoMethods.onWhichStage(gps);
                stageName = fileObj.getString("name");
                int parts = fileObj.getInt("parts");
                JSONObject main = fileObj.getJSONObject("main");
                int highlightCount = 0;
                if (onStageLocationData != null && parts > 1) {
                        JSONObject alt = fileObj.getJSONObject("alt");
                        for (int i = 0; i < parts; i++) {
                            JSONArray ar = new JSONArray();
                            if (i == onStageLocationData.partId){
                                if (onStageLocationData.alt && alt.has(i+"")) {
                                    ar = alt.getJSONArray(i + "");
                                } else {
                                    ar = main.getJSONArray(i + "");
                                }
                            } else {
                                ar = main.getJSONArray(i + "");
                            }
                            for (int j = 0; j < ar.length(); j++)  {
                                jsonArr.put(ar.getJSONObject(j));
                                    if (onStageLocationData.stageId == params[0]) {
                                        near = true;
                                        if (i <= onStageLocationData.partId) {
                                            if (i == onStageLocationData.partId && j == onStageLocationData.pointId) {
                                                highlightCount++;
                                                highlightId = highlightCount;
                                            } else {
                                                highlightCount++;
                                            }
                                        }
                                    }
                            }
                        }
                } else {
                    for (int i = 0; i < parts; i++) {
                        JSONArray ar = main.getJSONArray(i + "");
                        for (int j = 0; j < ar.length(); j++)  {
                            jsonArr.put(ar.getJSONObject(j));
                        }
                    }
                }

                JSONObject geo;
                JSONObject prev_geopoint = new JSONObject();
                double dist = 0;
                double minDist = 999;

                mStageKmLeft = 0;
                for (int h = 0; h < jsonArr.length(); h++) {
                    geo = jsonArr.getJSONObject(h);
                    double lat = geo.getDouble("lat");
                    double lng = geo.getDouble("lng");

                    float alt = (float) geo.getDouble("ele");
                    double prev_lat = lat;
                    double prev_lng = lng;
                    if (h == 0) {
                        dist = 0;
                    } else if (h == jsonArr.length() - 1) {
                        finish_latitude = lat;
                        finish_longitude = lng;
                    } else {
                        prev_lat = prev_geopoint.getDouble("lat");
                        prev_lng = prev_geopoint.getDouble("lng");
                        dist = dist + geoMethods.distance(new LatLng(prev_lat, prev_lng), new LatLng(lat, lng));
                    }

                    xVals.add(String.format("%.1f", dist));
                    yVals.add(new Entry(alt, h));

                    if (near) {
                        if (h > highlightId) {
                            mStageKmLeft = mStageKmLeft + geoMethods.distance(new LatLng(prev_lat, prev_lng), new LatLng(lat, lng));
                        }
                    }
                    prev_geopoint = geo;
                }
                mStageLength = dist;
                if (!near) {
                    mStageKmLeft = mStageLength;
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            addDataSet(xVals, yVals);
            mChart.invalidate();
//            tvStagelength.setText(String.format("%.1f", mStageLength) + "km");
            if (near) {
                mChart.highlightValue(highlightId, 0);
            }
            mStageKmDone = mStageLength - mStageKmLeft;
            tvKmDone.setText(String.format("%.1f", mStageKmDone) + "/" + String.format("%.1f", mStageLength) + " KM" + " (" + Math.round(mStageKmDone / mStageLength * 100) + "%)");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
