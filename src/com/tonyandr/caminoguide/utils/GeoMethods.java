package com.tonyandr.caminoguide.utils;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.tonyandr.caminoguide.constants.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Tony on 11-Feb-15.
 */
public class GeoMethods implements AppConstants {
    Context context;
    DBControllerAdapter dbController;
    public GeoMethods(Context context) {
        this.context = context;
        dbController = DBControllerAdapter.getInstance(context);
    }
    JsonFilesHandler jfh;
    // Location near our stage?
    public OnStageLocationData onWhichStage(Location location) throws JSONException {
        if (location != null) {
            JSONObject fileObj, max, min;
            ArrayList<Integer> stages = new ArrayList<>();

            jfh = new JsonFilesHandler(context);
            int stage_id = dbController.getStageFromLocation(location);
            if (stage_id == 0) {
                JSONArray jsonArray = jfh.parseJSONArr("json/bounds.json");
                for (int i = 0; i < jsonArray.length(); i++) {
                    fileObj = jsonArray.getJSONObject(i);
                    max = fileObj.getJSONObject("maxlatlng");
                    min = fileObj.getJSONObject("minlatlng");
                    LatLngBounds box = new LatLngBounds((new LatLng(min.getDouble("lat"), min.getDouble("lng"))),(new LatLng(max.getDouble("lat"), max.getDouble("lng"))));
                    if (box.contains(new LatLng(location.getLatitude(), location.getLongitude()))) {
                        stages.add(fileObj.getInt("stageid"));
                    }
                }
            } else {
                stages.add(stage_id);
            }

            if (stages.size() > 0) {

                double localMin = 9999;
                int localPointId = 0, partId = 0;
                boolean alt = false;
                for (int id:stages) {
                    fileObj = jfh.parseJSONObj("json/stage" + id + ".json");
                    OnStageLocationData onStageLocationData = getMinFromStage(fileObj, location);
                    double thisMin = onStageLocationData.localMin;
                    if(localMin > thisMin) {
                        localMin = thisMin;
                        stage_id = id;
                        partId = onStageLocationData.partId;
                        localPointId = onStageLocationData.pointId;
                        alt = onStageLocationData.alt;
                    }
                }
                return new OnStageLocationData(stage_id, partId, localPointId, alt);
            }
        }
        return null;
    }

    public Integer onWhichStageSimple(Location location) throws JSONException {
        if (location != null) {
            JSONObject fileObj, max, min;

            jfh = new JsonFilesHandler(context);
            int stage_id = 0;
            JSONArray jsonArray = jfh.parseJSONArr("json/bounds.json");
            for (int i = 0; i < jsonArray.length(); i++) {
                fileObj = jsonArray.getJSONObject(i);
                max = fileObj.getJSONObject("maxlatlng");
                min = fileObj.getJSONObject("minlatlng");
                LatLngBounds box = new LatLngBounds((new LatLng(min.getDouble("lat"), min.getDouble("lng"))),(new LatLng(max.getDouble("lat"), max.getDouble("lng"))));
                if (box.contains(new LatLng(location.getLatitude(), location.getLongitude()))) {
                    return fileObj.getInt("stageid");
                }
            }
        }
        return 0;
    }

    private OnStageLocationData getMinFromStage (JSONObject jsonObject, Location location) throws JSONException {
        double localMin = 9999;
        int pointId = 0, partId = 0;
        boolean alt_way = false;
        OnStageLocationData data;
        int parts = jsonObject.getInt("parts");
        JSONObject main = jsonObject.getJSONObject("main");
        for (int i=0; i<parts;i++){
            JSONArray part = main.getJSONArray(i+"");
            data = getMinFromArray(part,location);
            if (localMin > data.localMin) {
                localMin = data.localMin;
                pointId = data.pointId;
                partId = i;
                alt_way = false;
            }
        }
        if (parts > 1) {
            JSONObject alt = jsonObject.getJSONObject("alt");
            for (int i=0; i<parts;i++){
                if(alt.has(i+"")) {
                    JSONArray part = alt.getJSONArray(i+"");
                    data = getMinFromArray(part,location);
                    if (localMin > data.localMin) {
                        localMin = data.localMin;
                        pointId = data.pointId;
                        partId = i;
                        alt_way = true;
                    }
                }
            }
        }

        return new OnStageLocationData(localMin, partId, pointId, alt_way);
    }
    private OnStageLocationData getMinFromArray (JSONArray jsonArray, Location location) throws JSONException {
        double localMin = 9999;
        int id = 0;
        for (int i=0; i<jsonArray.length(); i++) {
            double lat = Double.parseDouble(jsonArray.getJSONObject(i).getString("lat"));
            double lng = Double.parseDouble(jsonArray.getJSONObject(i).getString("lng"));
            double dist = distance(new LatLng(lat, lng), new LatLng(location.getLatitude(), location.getLongitude()));
            if ( localMin > dist) {
                localMin = dist;
                id = i;
            }
        }
        return new OnStageLocationData(localMin, id);
    }


    public double distance(LatLng current, LatLng last){
        if(last==null)
            return 0;
        Location cL = new Location("");
        cL.setLatitude(current.latitude);
        cL.setLongitude(current.longitude);

        Location lL = new Location("");
        lL.setLatitude(last.latitude);
        lL.setLongitude(last.longitude);

        return lL.distanceTo(cL)/1000;
    }

}
