package com.tonyandr.caminoguide.utils;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tony on 31-Jan-15.
 */
public class JsonFilesHandler {
    Context mContext;

    public JsonFilesHandler(Context mContext) {
        this.mContext = mContext;
    }

    public JSONArray parseJSONArr(String filename) {
        String JSONString = null;
        JSONArray JSONArray = null;
        try {
            AssetManager assetManager = mContext.getAssets();
            //open the inputStream to the file
            InputStream inputStream = assetManager.open(filename);

            int sizeOfJSONFile = inputStream.available();

            //array that will store all the data
            byte[] bytes = new byte[sizeOfJSONFile];

            //reading data into the array from the file
            inputStream.read(bytes);

            //close the input stream
            inputStream.close();

            JSONString = new String(bytes, "UTF-8");
            JSONArray = new JSONArray(JSONString);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        catch (JSONException x) {
            x.printStackTrace();
            return null;
        }
        return JSONArray;
    }
    public JSONObject parseJSONObj(String filename) {
        String JSONString = null;
        JSONObject JSONObject = null;
        try {

            AssetManager assetManager = mContext.getAssets();
            //open the inputStream to the file
            InputStream inputStream = assetManager.open(filename);

            int sizeOfJSONFile = inputStream.available();

            //array that will store all the data
            byte[] bytes = new byte[sizeOfJSONFile];

            //reading data into the array from the file
            inputStream.read(bytes);

            //close the input stream
            inputStream.close();

            JSONString = new String(bytes, "UTF-8");
            JSONObject = new JSONObject(JSONString);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        catch (JSONException x) {
            x.printStackTrace();
            return null;
        }
        return JSONObject;
    }

}
