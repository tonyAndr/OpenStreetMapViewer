package com.tonyandr.caminoguide.utils;

import android.app.IntentService;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tony on 14-Feb-15.
 */
public class DBUpdateService extends IntentService {
    private DBControllerAdapter dbController;
    private int albArrayLength;

    public DBUpdateService() {
        super("DBUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dbController = DBControllerAdapter.getInstance(this);
        eraseAlbergues();
        eraseLocalities();
        try {
            if (intent.getStringExtra("albergues") != null) {
                JSONArray albergues = new JSONArray(intent.getStringExtra("albergues"));
                albArrayLength = albergues.length();
                addAlbergues(albergues);
            }
            if (intent.getStringExtra("localities") != null) {
                JSONArray localities = new JSONArray(intent.getStringExtra("localities"));
                addLocalities(localities);
            }
            if (intent.getStringExtra("hotels") != null) {
                JSONArray localities = new JSONArray(intent.getStringExtra("hotels"));
                addLocalities(localities);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendThisBroadcast(100, albArrayLength, true);

    }

    private void sendThisBroadcast(int progress, int count, boolean finished) {
        Intent intent = new Intent();
        intent.setAction("DBUpdateService");
        intent.putExtra("finished", finished);
        intent.putExtra("progress", progress);
        intent.putExtra("count", count);
        sendBroadcast(intent);
    }

    private void addAlbergues (JSONArray array) {
        JSONObject obj;
        long id;
        String[] location;
        int count = 0;
        int length = array.length();
        int progress = 0;
        try {
            for (int i = 0; i < length; i++) {
                obj = array.getJSONObject(i);
                location = new String[2];

                if (obj.getString("alb_geo").contains(", ")) {
                    location = obj.getString("alb_geo").split(", ");
                } else if (obj.getString("alb_geo").contains(",")) {
                    location = obj.getString("alb_geo").split(",");
                } else {
                    location = null;
                }
                if (location !=null) {
                    id = dbController.insertAlbergue(obj.getString("albergue"), obj.getString("locality"),
                            obj.getString("telephone"), obj.getString("beds"), obj.getInt("stage"),
                            Double.parseDouble(location[0]), Double.parseDouble(location[1]),
                            obj.getString("type"), obj.getString("address"));
                    count++;
                    progress = Math.round(count*100/length);
                    sendThisBroadcast(progress, count, false);
                } else {
//                    Log.d("dbservice", "Null at " + obj.getInt("id"));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void addLocalities (JSONArray array) {
        JSONObject obj;
        int count = 0;
        try {
            for (int i = 0; i < array.length(); i++) {
                obj = array.getJSONObject(i);
                    dbController.insertLocality(obj.getString("locality"), obj.getDouble("latitude"), obj.getDouble("longitude"));
                    count++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void eraseAlbergues () {
        int count = dbController.eraseAlbergues();
    }
    private void eraseLocalities () {
        int count = dbController.eraseLocality();
    }

}
