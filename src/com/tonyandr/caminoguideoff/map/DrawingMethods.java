package com.tonyandr.caminoguideoff.map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tonyandr.caminoguideoff.R;
import com.tonyandr.caminoguideoff.constants.AppConstants;
import com.tonyandr.caminoguideoff.utils.DBControllerAdapter;
import com.tonyandr.caminoguideoff.utils.GMapReturnObject;
import com.tonyandr.caminoguideoff.utils.GeoMethods;
import com.tonyandr.caminoguideoff.utils.JsonFilesHandler;
import com.tonyandr.caminoguideoff.utils.MarkerDataObject;
import com.tonyandr.caminoguideoff.utils.OnStageLocationData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tony on 11-Feb-15.
 */
public class DrawingMethods implements AppConstants {
    private MapView mapView;
    private Context context;
    JsonFilesHandler jfh;
    GeoMethods geoMethods;
    DBControllerAdapter dbController;
    private OnStageLocationData currentData;
    private OnStageLocationData finishData;
    private JSONArray returnArray;

    public DrawingMethods(MapView mapView, Context context) {
        this.context = context;
        this.mapView = mapView;
        jfh = new JsonFilesHandler(context);
        dbController = DBControllerAdapter.getInstance(context);
        geoMethods = new GeoMethods(context);
    }

    public DrawingMethods(Context context) {
        this.context = context;
        jfh = new JsonFilesHandler(context);
        dbController = DBControllerAdapter.getInstance(context);
        geoMethods = new GeoMethods(context);
    }

    static Paint createPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    public Marker createTappableMarker(final Context c, final String title, int resourceIdentifier,
                                              final LatLong latLong) {
        Drawable drawable = c.getResources().getDrawable(resourceIdentifier);
        final Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        bitmap.incrementRefCount();

        return new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2) {
            @Override
            public boolean onTap(LatLong geoPoint, Point viewPosition,
                                 Point tapPoint) {
                if (contains(viewPosition, tapPoint)) {
                    Toast.makeText(c,
                            title,
                            Toast.LENGTH_SHORT).show();
                    mapView.getModel().mapViewPosition.animateTo(latLong);
                    return true;
                }
                return false;
            }
        };
    }

    // *** Get array of geopoints to osm and gmap
    public JSONArray getRouteArray(Location current, Location finish) throws JSONException {
        returnArray = new JSONArray();
        String current_type, finish_type;
        finishData = geoMethods.onWhichStage(finish);
        if (finishData != null) {
            finish_type = (finishData.alt ? "alt" : "main");
            currentData = geoMethods.onWhichStage(current);
            Log.e("loc", "loc : "+current);
//            Log.e("loc", "stage "+currentData.stageId);

            if (currentData != null) {
                current_type = (currentData.alt ? "alt" : "main");
                if (currentData.stageId == finishData.stageId) {
                    JSONObject jsonObject = jfh.parseJSONObj("json/stage" + currentData.stageId + ".json");
                    if (currentData.partId == finishData.partId) {
                        if (currentData.alt == finishData.alt) {
                            pointSamePartSameAlt(jsonObject, current_type);
                        } else {
                            pointSamePartDiffAlt(jsonObject, current_type, finish_type);
                        }
                    } else if (currentData.partId > finishData.partId) {
                        pointDiffPartBackward(jsonObject, current_type, finish_type);
                    } else if (currentData.partId < finishData.partId) {
                        pointDiffPartForward(jsonObject, current_type, finish_type);
                    }
                } else if (currentData.stageId > finishData.stageId) {
                    pointDiffStageBackward(current_type, finish_type);
                } else if (currentData.stageId < finishData.stageId) {
                    pointDiffStageForward(current_type, finish_type);
                }
            } else {
                JSONObject jsonObject = jfh.parseJSONObj("json/stage" + finishData.stageId + ".json");
                for (int i = 0; i < jsonObject.getInt("parts"); i++) {
                    if (i == finishData.partId) {
                        returnArray = jsonObject.getJSONObject(finish_type).getJSONArray(finishData.partId + "");
                    } else {
                        returnArray = jsonObject.getJSONObject("main").getJSONArray(i + "");
                    }
                }
            }
        }
        return returnArray;
    }

    private void pointSamePartSameAlt(JSONObject obj, String type) throws JSONException {
        JSONArray arr = obj.getJSONObject(type).getJSONArray(currentData.partId + "");
        if (currentData.pointId > finishData.pointId) {
            for (int i = currentData.pointId; i >= finishData.pointId; i--) {
                returnArray.put(arr.getJSONObject(i));
            }
        } else if (currentData.pointId < finishData.pointId) {
            for (int i = currentData.pointId; i <= finishData.pointId; i++) {
                returnArray.put(arr.getJSONObject(i));
            }
        } else {

        }
    }

    private void pointSamePartDiffAlt(JSONObject obj, String cur_type, String fin_type) throws JSONException {
        JSONArray cur = obj.getJSONObject(cur_type).getJSONArray(currentData.partId + "");
        JSONArray fin = obj.getJSONObject(fin_type).getJSONArray(finishData.partId + "");
        JSONArray backward = new JSONArray();
        JSONArray forward = new JSONArray();
        double back_dist = 0, forw_dist = 0;
        JSONObject p_ob = new JSONObject(); // previos point

        if (currentData.partId == 0) {
            for (int i = currentData.pointId; i < cur.length(); i++) {
                returnArray.put(cur.getJSONObject(i));
            }
            for (int i = fin.length() - 1; i >= finishData.pointId; i--) {
                returnArray.put(fin.getJSONObject(i));
            }
        } else if (currentData.partId == obj.getInt("parts") - 1) {
            for (int i = currentData.pointId; i >= 0; i--) {
                returnArray.put(cur.getJSONObject(i));
            }
            for (int i = 0; i <= finishData.pointId; i++) {
                returnArray.put(fin.getJSONObject(i));
            }
        } else {
            for (int i = currentData.pointId; i >= 0; i--) {
                JSONObject ob = cur.getJSONObject(i);
                backward.put(ob);
                if (i != currentData.pointId) {
                    back_dist = back_dist + geoMethods.distance(new LatLng(ob.getDouble("lat"), ob.getDouble("lng")), new LatLng(p_ob.getDouble("lat"), p_ob.getDouble("lng")));
                } else {
                    p_ob = ob;
                }
            }
            for (int i = 0; i <= finishData.pointId; i++) {
                JSONObject ob = fin.getJSONObject(i);
                backward.put(ob);
                if (i != 0) {
                    back_dist = back_dist + geoMethods.distance(new LatLng(ob.getDouble("lat"), ob.getDouble("lng")), new LatLng(p_ob.getDouble("lat"), p_ob.getDouble("lng")));
                } else {
                    p_ob = ob;
                }
            }

            for (int i = currentData.pointId; i < cur.length(); i++) {
                JSONObject ob = cur.getJSONObject(i);
                forward.put(ob);
                if (i != currentData.pointId) {
                    forw_dist = forw_dist + geoMethods.distance(new LatLng(ob.getDouble("lat"), ob.getDouble("lng")), new LatLng(p_ob.getDouble("lat"), p_ob.getDouble("lng")));
                } else {
                    p_ob = ob;
                }
            }
            for (int i = fin.length() - 1; i >= finishData.pointId; i--) {
                JSONObject ob = fin.getJSONObject(i);
                forward.put(ob);
                if (i != fin.length() - 1) {
                    forw_dist = forw_dist + geoMethods.distance(new LatLng(ob.getDouble("lat"), ob.getDouble("lng")), new LatLng(p_ob.getDouble("lat"), p_ob.getDouble("lng")));
                } else {
                    p_ob = ob;
                }
            }

            if (back_dist < forw_dist) {
                for (int i = 0; i < backward.length(); i++) {
                    returnArray.put(backward.getJSONObject(i));
                }
            } else {
                for (int i = 0; i < forward.length(); i++) {
                    returnArray.put(forward.getJSONObject(i));
                }
            }
        }
    }

    private void pointDiffPartBackward(JSONObject obj, String cur_type, String fin_type) throws JSONException {
        ArrayList<JSONArray> list = new ArrayList<>();
        list.add(obj.getJSONObject(cur_type).getJSONArray(currentData.partId + ""));
        if ((currentData.partId - finishData.partId) > 1) {
            for (int i = currentData.partId - 1; i > finishData.partId; i--) {
                list.add(obj.getJSONObject("main").getJSONArray(i + ""));
            }
        }

        list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
        for (int i = 0; i < list.size(); i++) {
            JSONArray ar = list.get(i);
            if (i == 0) {
                for (int j = currentData.pointId; j >= 0; j--) {
                    returnArray.put(ar.getJSONObject(j));
                }
            } else if (i == list.size() - 1) {
                for (int j = ar.length() - 1; j >= finishData.pointId; j--) {
                    returnArray.put(ar.getJSONObject(j));
                }
            } else {
                for (int j = ar.length() - 1; j >= 0; j--) {
                    returnArray.put(ar.getJSONObject(j));
                }
            }
        }
    }

    private void pointDiffPartForward(JSONObject obj, String cur_type, String fin_type) throws JSONException {
        ArrayList<JSONArray> list = new ArrayList<>();
        list.add(obj.getJSONObject(cur_type).getJSONArray(currentData.partId + ""));
        if ((finishData.partId - currentData.partId) > 1) {
            for (int i = (currentData.partId + 1); i < finishData.partId; i++) {
                list.add(obj.getJSONObject("main").getJSONArray(i + ""));
            }
        }
        list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
        for (int i = 0; i < list.size(); i++) {
            JSONArray ar = list.get(i);
            if (i == 0) {
                for (int j = currentData.pointId; j < ar.length(); j++) {
                    returnArray.put(ar.getJSONObject(j));
                }
            } else if (i == list.size() - 1) {
                for (int j = 0; j <= finishData.pointId; j++) {
                    returnArray.put(ar.getJSONObject(j));
                }
            } else {
                for (int j = 0; j < ar.length(); j++) {
                    returnArray.put(ar.getJSONObject(j));
                }
            }
        }
    }

    private boolean stageHasAltPart(JSONObject obj, int index) throws JSONException {
        boolean hasAlt = false;
        boolean hasPart = false;
        hasAlt = obj.has("alt");
        if (hasAlt) {
            hasPart = obj.getJSONObject("alt").has(index+"");
        }
        return hasPart;
    }

    private void pointDiffStageBackward(String cur_type, String fin_type) throws JSONException {
        ArrayList<JSONArray> list = new ArrayList<>();
        boolean diff_alts = false;
        for (int i = currentData.stageId; i >= finishData.stageId; i--) {
            JSONObject obj = jfh.parseJSONObj("json/stage" + i + ".json");
            if (i == currentData.stageId) {
                list.add(obj.getJSONObject(cur_type).getJSONArray(currentData.partId + ""));
                if (currentData.partId != 0) {
                    for (int j = currentData.partId - 1; j >= 0; j--) {
                        list.add(obj.getJSONObject("main").getJSONArray(j + ""));
                    }
                }
            } else if (i == finishData.stageId) {
                if (obj.getInt("parts") > 1) {
                    if (finishData.partId != (obj.getInt("parts") - 1)) {
                        for (int j = obj.getInt("parts") - 1; j > finishData.partId; j--) {
                            if (j == (obj.getInt("parts") - 1) && currentData.alt && (i == (currentData.stageId - 1)) && stageHasAltPart(obj, j)) {
                                list.add(obj.getJSONObject("alt").getJSONArray(j + ""));
                            } else {
                                list.add(obj.getJSONObject("main").getJSONArray(j + ""));
                            }
                        }
                        list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                    } else {
                        if ((!cur_type.equals(fin_type) && (i == (currentData.stageId - 1)))) {
                            list.add(obj.getJSONObject(cur_type).getJSONArray(finishData.partId + ""));
                            list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                            diff_alts = true;
                        } else {
                            list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                        }
                    }

                } else {
                    list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                }
            } else {
                for (int j = obj.getInt("parts") - 1; j >= 0; j--) {
                    if (j == (obj.getInt("parts") - 1) && currentData.alt && (i == (currentData.stageId - 1)) && stageHasAltPart(obj, j)) {
                        list.add(obj.getJSONObject("alt").getJSONArray(j + ""));
                    } else {
                        list.add(obj.getJSONObject("main").getJSONArray(j + ""));
                    }
                }

            }
        }
        for (int i = 0; i < list.size(); i++) {
            JSONArray ar = list.get(i);
            if (i == 0) {
                for (int j = currentData.pointId; j >= 0; j--) {
                    returnArray.put(ar.getJSONObject(j));
                }
            } else if (i == list.size() - 1) {
                if (diff_alts) {
                    for (int j = 0; j <= finishData.pointId; j++) {
                        returnArray.put(ar.getJSONObject(j));
                    }
                } else {
                    for (int j = ar.length() - 1; j >= finishData.pointId; j--) {
                        returnArray.put(ar.getJSONObject(j));
                    }
                }
            } else {
                for (int j = ar.length() - 1; j >= 0; j--) {
                    returnArray.put(ar.getJSONObject(j));
                }
            }
        }
    }

    private void pointDiffStageForward(String cur_type, String fin_type) throws JSONException {
        ArrayList<JSONArray> list = new ArrayList<>();
        boolean diff_alts = false;
        for (int i = currentData.stageId; i <= finishData.stageId; i++) {
            JSONObject obj = jfh.parseJSONObj("json/stage" + i + ".json");
            if (i == currentData.stageId) {
                list.add(obj.getJSONObject(cur_type).getJSONArray(currentData.partId + ""));
                if (currentData.partId != (obj.getInt("parts") - 1)) {
                    for (int j = currentData.partId + 1; j < obj.getInt("parts"); j++) {
                        list.add(obj.getJSONObject("main").getJSONArray(j + ""));
                    }
                }
            } else if (i == finishData.stageId) {
                if (obj.getInt("parts") > 1) {
                    if (finishData.partId != 0) {
                        for (int j = 0; j < finishData.partId; j++) {
                            if (j == 0 && currentData.alt && (i == (currentData.stageId + 1)) && stageHasAltPart(obj, j)) {
                                list.add(obj.getJSONObject("alt").getJSONArray(j + ""));
                            } else {
                                list.add(obj.getJSONObject("main").getJSONArray(j + ""));
                            }
                        }
                        list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                    } else {
                        if ((!cur_type.equals(fin_type) && (i == (currentData.stageId + 1)))) {
                            list.add(obj.getJSONObject(cur_type).getJSONArray(finishData.partId + ""));
                            list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                            diff_alts = true;
                        } else {
                            list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                        }
                    }
                } else {
                    list.add(obj.getJSONObject(fin_type).getJSONArray(finishData.partId + ""));
                }
            } else {
                for (int j = 0; j < obj.getInt("parts"); j++) {
                    if (j == 0 && currentData.alt && (i == (currentData.stageId + 1)) && stageHasAltPart(obj, j)) {
                        list.add(obj.getJSONObject("alt").getJSONArray(j + ""));
                    } else {
                        list.add(obj.getJSONObject("main").getJSONArray(j + ""));
                    }
                }
            }
        }
        for (int i = 0; i < list.size(); i++) {
            JSONArray ar = list.get(i);
            if (i == 0) {
                for (int j = currentData.pointId; j < ar.length(); j++) {
                    returnArray.put(ar.getJSONObject(j));
                }
            } else if (i == list.size() - 1) {
                if (diff_alts) {
                    for (int j = ar.length() - 1; j >= finishData.pointId; j--) {
                        returnArray.put(ar.getJSONObject(j));
                    }
                } else {
                    for (int j = 0; j <= finishData.pointId; j++) {
                        returnArray.put(ar.getJSONObject(j));
                    }
                }
            } else {
                for (int j = 0; j < ar.length(); j++) {
                    returnArray.put(ar.getJSONObject(j));
                }
            }
        }
    }



    // *** GMAP Functions

    public ArrayList<MarkerDataObject> drawAlbMarkersGMAP(Location finish) throws JSONException {
        ArrayList<MarkerDataObject> markers = new ArrayList<>();
        boolean highlighted = false;
        JSONObject vh = null;
        JSONArray albJArr = dbController.getAlbergues(0);
        for (int i = 0; i < albJArr.length(); i++) {
            JSONObject v = albJArr.getJSONObject(i);
            String title = "Albergue " + v.getString("title");
            String snippet = v.getString("tel");
            Double lat = v.getDouble("lat");
            Double lng = v.getDouble("lng");
            if (finish != null) {
                if (lat == finish.getLatitude() && lng == finish.getLongitude()) {
                    highlighted = true;
                    vh = v;
                } else {
                    markers.add(new MarkerDataObject(new LatLng(lat, lng), title, snippet, R.drawable.ic_albergue_marker_green));
                }
            } else {
                markers.add(new MarkerDataObject(new LatLng(lat, lng), title, snippet, R.drawable.ic_albergue_marker_green));

            }
        }
        if (highlighted && vh != null) {
            Double lat = vh.getDouble("lat");
            Double lng = vh.getDouble("lng");
            String title = "Albergue " + vh.getString("title");
            String snippet = vh.getString("tel");
            markers.add(new MarkerDataObject(new LatLng(lat, lng), title, snippet, R.drawable.ic_albergue_marker_red));
        }

        return markers;
    }

    public ArrayList<MarkerDataObject> drawCityMarkersGMAP() throws JSONException {
        ArrayList<MarkerDataObject> markers = new ArrayList<>();
        JSONArray routeJArr = dbController.getLocalities();
        for (int i = 0; i < routeJArr.length(); i++) {
            JSONObject v = routeJArr.getJSONObject(i);
            if (v != null) {
                Double lat = v.getDouble("lat");
                Double lng = v.getDouble("lng");
                String title = v.getString("title");
                markers.add(new MarkerDataObject(new LatLng(lat, lng), title, null, R.drawable.ic_locality_marker));
            }
        }
        return markers;
    }

    public ArrayList<PolylineOptions> drawAllRouteGMAP(int stage) throws JSONException {
        PolylineOptions rectOptionsStart = new PolylineOptions();
        PolylineOptions rectOptionsFinish = new PolylineOptions();
        PolylineOptions rectOptionsHighlight = new PolylineOptions();
        PolylineOptions rectOptionsAlt = new PolylineOptions();
        ArrayList<PolylineOptions> returnList = new ArrayList<PolylineOptions>();
        boolean finish = false;

        //stage == 0 => all route w/o highlight
        JSONObject fileObj, geo;
        JSONArray geoArr;
        LatLng newPoint;
        for (int i = 1; i < 32; i++) {
            fileObj = jfh.parseJSONObj("json/stage" + i + ".json");
            if (fileObj.getInt("parts") > 1) {
                for (int j = 0; j < fileObj.getInt("parts"); j++) {
                    JSONArray ar = fileObj.getJSONObject("main").getJSONArray(j + "");
                    for (int h = 0; h < ar.length(); h++) {
                        geo = ar.getJSONObject(h);
                        Double lat = geo.getDouble("lat");
                        Double lng = geo.getDouble("lng");
                        newPoint = new LatLng(lat, lng);
                        if (i == stage) {
                            rectOptionsHighlight.add(newPoint);
                            finish = true;
                        } else {
                            if (finish) {
                                rectOptionsFinish.add(newPoint);
                            } else {
                                rectOptionsStart.add(newPoint);
                            }
                        }
                    }
                    if (fileObj.getJSONObject("alt").has(j + "")) {
                        rectOptionsAlt = new PolylineOptions();
                        JSONArray ar_alt = fileObj.getJSONObject("alt").getJSONArray(j + "");
                        for (int h = 0; h < ar_alt.length(); h++) {
                            geo = ar_alt.getJSONObject(h);
                            Double lat = geo.getDouble("lat");
                            Double lng = geo.getDouble("lng");
                            newPoint = new LatLng(lat, lng);
                            rectOptionsAlt.add(newPoint);
                        }
                        rectOptionsAlt.color(Color.argb(200, 255, 255, 0)).width(5).geodesic(true);
                        returnList.add(rectOptionsAlt);
                    }
                }
            } else {
                JSONArray ar = fileObj.getJSONObject("main").getJSONArray("0");
                for (int h = 0; h < ar.length(); h++) {
                    geo = ar.getJSONObject(h);
                    Double lat = geo.getDouble("lat");
                    Double lng = geo.getDouble("lng");
                    newPoint = new LatLng(lat, lng);
                    if (i == stage) {
                        rectOptionsHighlight.add(newPoint);
                        finish = true;
                    } else {
                        if (finish) {
                            rectOptionsFinish.add(newPoint);
                        } else {
                            rectOptionsStart.add(newPoint);
                        }
                    }
                }
            }
        }
        rectOptionsStart.color(Color.argb(200, 0, 150, 136)).width(6).geodesic(true);
        rectOptionsFinish.color(Color.argb(200, 0, 150, 136)).width(6).geodesic(true);
        rectOptionsHighlight.color(Color.argb(255, 244, 68, 68)).width(6).geodesic(true);
        returnList.add(rectOptionsStart);
        returnList.add(rectOptionsHighlight);
        returnList.add(rectOptionsFinish);
        return returnList;
    }

    public GMapReturnObject drawDistanceRouteGMAP(Location current, Location finish) throws JSONException {
        PolylineOptions rectOptions = new PolylineOptions();

        JSONArray geoArr = getRouteArray(current, finish);
        JSONObject geopoint;
        JSONObject prev_geopoint = new JSONObject();
        double distFromCurrToFin = 0;
        if (currentData != null) {
//            if (areaLimitSpainGMap.contains(new LatLng(current.getLatitude(), current.getLongitude())))
            rectOptions.add(new LatLng(current.getLatitude(), current.getLongitude()));
        }
        if (geoArr.length() > 0) {
            for (int h = 0; h < geoArr.length(); h++) {
                geopoint = geoArr.getJSONObject(h);
                if (h == 0) {
                    if (current != null)
                        distFromCurrToFin = distFromCurrToFin + geoMethods.distance(new LatLng(geopoint.getDouble("lat"), geopoint.getDouble("lng")), new LatLng(current.getLatitude(), current.getLongitude()));
                    rectOptions.add(new LatLng(geopoint.getDouble("lat"), geopoint.getDouble("lng")));
                } else {
                    distFromCurrToFin = distFromCurrToFin + geoMethods.distance(
                            new LatLng(prev_geopoint.getDouble("lat"), prev_geopoint.getDouble("lng")),
                            new LatLng(geopoint.getDouble("lat"), geopoint.getDouble("lng")));
                    rectOptions.add(new LatLng(geopoint.getDouble("lat"), geopoint.getDouble("lng")));

                }
                prev_geopoint = geopoint;
            }
            distFromCurrToFin = distFromCurrToFin + geoMethods.distance(new LatLng(prev_geopoint.getDouble("lat"), prev_geopoint.getDouble("lng")), new LatLng(finish.getLatitude(), finish.getLongitude()));
        }
        if (currentData != null) {
            rectOptions.add(new LatLng(finish.getLatitude(), finish.getLongitude()));
        }
        rectOptions.color(Color.argb(230, 244, 68, 68)).width(6).geodesic(true);

        return new GMapReturnObject(distFromCurrToFin, rectOptions);
    }


    //** MapsForge


    public void drawCityMarkersMF(Layers layers) throws JSONException {

        Bitmap bubble;

        JSONArray routeJArr = dbController.getLocalities();
        for (int i = 0; i < routeJArr.length(); i++) {
            JSONObject v = routeJArr.getJSONObject(i);
            if (v != null) {
                Double lat = v.getDouble("lat");
                Double lng = v.getDouble("lng");
                String title = v.getString("title");
                org.mapsforge.map.layer.overlay.Marker marker = createTappableMarker(context, title, R.drawable.ic_locality_marker, new LatLong(lat, lng));

                layers.add(marker);
            }
        }
    }

    public void drawAlbMarkersMF(Layers layers, Location finish) throws JSONException {
        boolean highlighted = false;
        JSONObject vh = null;
        String title_high = "";
        JSONArray albJArr = dbController.getAlbergues(0);
        for (int i = 0; i < albJArr.length(); i++) {
            JSONObject v = albJArr.getJSONObject(i);
            String title = "Albergue " + v.getString("title");
            String desc = v.getString("locality") + " " + v.getString("address");
            String snippet = v.getString("tel");
            Double lat = v.getDouble("lat");
            Double lng = v.getDouble("lng");
            if (finish != null) {
                if (lat == finish.getLatitude() && lng == finish.getLongitude()) {
                    highlighted = true;
                    vh = v;
                    title_high = title;
                } else {
                    org.mapsforge.map.layer.overlay.Marker marker = createTappableMarker(context, title, R.drawable.ic_albergue_marker_green, new LatLong(lat, lng));
                    layers.add(marker);
                }
            } else {
                org.mapsforge.map.layer.overlay.Marker marker = createTappableMarker(context, title, R.drawable.ic_albergue_marker_green, new LatLong(lat, lng));
                layers.add(marker);
            }
        }
        if (highlighted && vh != null) {
            org.mapsforge.map.layer.overlay.Marker marker = createTappableMarker(context, title_high, R.drawable.ic_albergue_marker_red, new LatLong(finish.getLatitude(), finish.getLongitude()));
            layers.add(marker);
        }
    }

    public ArrayList<org.mapsforge.map.layer.overlay.Polyline> drawAllRouteMF(int stage) throws JSONException {
        Paint paintStrokeAlt = createPaint(Color.rgb(255, 255, 0), 1,
                Style.STROKE);
        paintStrokeAlt.setDashPathEffect(new float[] { 8, 8 });
        paintStrokeAlt.setStrokeWidth(4);

        Paint paintStroke = createPaint(Color.rgb(0, 150, 136), 1,
                Style.STROKE);
        paintStroke.setStrokeWidth(5);
        Paint paintStrokeHigh = createPaint(Color.rgb(244,68,68), 1,
                Style.STROKE);
        paintStrokeHigh.setStrokeWidth(5);

        org.mapsforge.map.layer.overlay.Polyline polylineStart = new org.mapsforge.map.layer.overlay.Polyline(paintStroke,AndroidGraphicFactory.INSTANCE);
        org.mapsforge.map.layer.overlay.Polyline polylineFinish = new org.mapsforge.map.layer.overlay.Polyline(paintStroke,AndroidGraphicFactory.INSTANCE);
        org.mapsforge.map.layer.overlay.Polyline polylineHighlight = new org.mapsforge.map.layer.overlay.Polyline(paintStrokeHigh,AndroidGraphicFactory.INSTANCE);
        org.mapsforge.map.layer.overlay.Polyline polylineAlt = new org.mapsforge.map.layer.overlay.Polyline(paintStrokeAlt,AndroidGraphicFactory.INSTANCE);
        ArrayList<org.mapsforge.map.layer.overlay.Polyline> returnList = new ArrayList<org.mapsforge.map.layer.overlay.Polyline>();

        List<LatLong> listPolylineStart = polylineStart.getLatLongs();
        List<LatLong> listPolylineFinish = polylineFinish.getLatLongs();
        List<LatLong> listPolylineAlt = polylineAlt.getLatLongs();
        List<LatLong> listPolylineHigh = polylineHighlight.getLatLongs();

        boolean finish = false;
        //stage == 0 => all route w/o highlight
        JSONObject fileObj, geo;
        JSONArray geoArr;
        LatLong newPoint;
        for (int i = 1; i < 32; i++) {
            fileObj = jfh.parseJSONObj("json/stage" + i + ".json");
            if (fileObj.getInt("parts") > 1) {
                for (int j = 0; j < fileObj.getInt("parts"); j++) {
                    JSONArray ar = fileObj.getJSONObject("main").getJSONArray(j + "");
                    for (int h = 0; h < ar.length(); h++) {
                        geo = ar.getJSONObject(h);
                        Double lat = geo.getDouble("lat");
                        Double lng = geo.getDouble("lng");
                        newPoint = new LatLong(lat, lng);
                        if (i == stage) {
                            listPolylineHigh.add(newPoint);
                            finish = true;
                        } else {
                            if (finish) {
                                listPolylineFinish.add(newPoint);
                            } else {
                                listPolylineStart.add(newPoint);
                            }
                        }
                    }
                    if (fileObj.getJSONObject("alt").has(j + "")) {
                        polylineAlt = new org.mapsforge.map.layer.overlay.Polyline(paintStrokeAlt,AndroidGraphicFactory.INSTANCE);
//                        listPolylineAlt.clear();
                        listPolylineAlt = polylineAlt.getLatLongs();
                        JSONArray ar_alt = fileObj.getJSONObject("alt").getJSONArray(j + "");
                        for (int h = 0; h < ar_alt.length(); h++) {
                            geo = ar_alt.getJSONObject(h);
                            Double lat = geo.getDouble("lat");
                            Double lng = geo.getDouble("lng");
                            newPoint = new LatLong(lat, lng);
                            listPolylineAlt.add(newPoint);
                        }
                        returnList.add(polylineAlt);
                    }
                }
            } else {
                JSONArray ar = fileObj.getJSONObject("main").getJSONArray("0");
                for (int h = 0; h < ar.length(); h++) {
                    geo = ar.getJSONObject(h);
                    Double lat = geo.getDouble("lat");
                    Double lng = geo.getDouble("lng");
                    newPoint = new LatLong(lat, lng);
                    if (i == stage) {
                        listPolylineHigh.add(newPoint);
                        finish = true;
                    } else {
                        if (finish) {
                            listPolylineFinish.add(newPoint);
                        } else {
                            listPolylineStart.add(newPoint);
                        }
                    }
                }
            }
        }
        returnList.add(polylineStart);
        returnList.add(polylineHighlight);
        returnList.add(polylineFinish);
        return returnList;
    }



    public double drawDistanceRouteMF(Layers layers, Location current, Location finish) throws JSONException {
        Log.e("loc", "in drawDiscanceRoute: " + current);
        Paint paintStroke = createPaint(Color.rgb(244,68,68), 1,
                Style.STROKE);
        paintStroke.setStrokeWidth(5);

        org.mapsforge.map.layer.overlay.Polyline polyline = new org.mapsforge.map.layer.overlay.Polyline(paintStroke,AndroidGraphicFactory.INSTANCE);
        List<LatLong> listPolyline = polyline.getLatLongs();

        JSONArray geoArr = getRouteArray(current, finish);
        JSONObject geopoint;
        JSONObject prev_geopoint = new JSONObject();
        double distFromCurrToFin = 0;
        if (currentData != null) {
//            if (areaLimitSpainGMap.contains(new LatLng(current.getLatitude(), current.getLongitude())))
            listPolyline.add(new LatLong(current.getLatitude(), current.getLongitude()));
        }
        if (geoArr.length() > 0) {
            for (int h = 0; h < geoArr.length(); h++) {
                geopoint = geoArr.getJSONObject(h);
                if (h == 0) {
                    if (current != null)
                        distFromCurrToFin = distFromCurrToFin + geoMethods.distance(new LatLng(geopoint.getDouble("lat"), geopoint.getDouble("lng")), new LatLng(current.getLatitude(), current.getLongitude()));
                    listPolyline.add(new LatLong(geopoint.getDouble("lat"), geopoint.getDouble("lng")));
                } else {
                    distFromCurrToFin = distFromCurrToFin + geoMethods.distance(
                            new LatLng(prev_geopoint.getDouble("lat"), prev_geopoint.getDouble("lng")),
                            new LatLng(geopoint.getDouble("lat"), geopoint.getDouble("lng")));
                    listPolyline.add(new LatLong(geopoint.getDouble("lat"), geopoint.getDouble("lng")));

                }
                prev_geopoint = geopoint;
            }
            distFromCurrToFin = distFromCurrToFin + geoMethods.distance(new LatLng(prev_geopoint.getDouble("lat"), prev_geopoint.getDouble("lng")), new LatLng(finish.getLatitude(), finish.getLongitude()));
        }
        if (currentData != null) {
            listPolyline.add(new LatLong(finish.getLatitude(), finish.getLongitude()));
        }
        layers.add(polyline);

        return distFromCurrToFin;
    }
}
