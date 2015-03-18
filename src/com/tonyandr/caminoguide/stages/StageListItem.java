package com.tonyandr.caminoguide.stages;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tony on 20-Jan-15.
 */
public class StageListItem {
    public Integer number;
    public String fromTo;
    public String fromToAlt;
    public boolean current = false;
    public boolean has_alt = false;


    public StageListItem(Integer number, String fromTo, String fromToAlt, boolean current, boolean has_alt) {
        this.number = number;
        this.fromTo = fromTo;
        this.fromToAlt = fromToAlt;
        this.current = current;
        this.has_alt = has_alt;
    }

    public StageListItem(JSONObject object) {
        try {
            this.number = object.getInt("number");
            this.fromTo = object.getString("fromTo");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
//    public static ArrayList<StageListItem> fromJson(JSONArray jsonObjects) {
//        ArrayList<StageListItem> stages = new ArrayList<StageListItem>();
//        for (int i = 0; i < jsonObjects.length(); i++) {
//            try {
//                stages.add(new StageListItem(jsonObjects.getJSONObject(i)));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return stages;
//    }
}