package com.tonyandr.caminoguide.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.tonyandr.caminoguide.constants.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Tony on 14-Feb-15.
 */
public class DBControllerAdapter implements AppConstants{

    DBController dbController;

    private static DBControllerAdapter mInstance = null;

    public static DBControllerAdapter getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new DBControllerAdapter(context.getApplicationContext());
        }
        return mInstance;
    }

    private DBControllerAdapter(Context context) {
        dbController = new DBController(context);
    }

    public long insertAlbergue(String title, String locality, String tel, String beds, int stage, double lat, double lng, String type, String address) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dbController.albergues.TITLE, title);
        cv.put(dbController.albergues.LOCALITY, locality);
        cv.put(dbController.albergues.TEL, tel);
        cv.put(dbController.albergues.BEDS, beds);
        cv.put(dbController.albergues.LAT, lat);
        cv.put(dbController.albergues.LNG, lng);
        cv.put(dbController.albergues.STAGE, stage);
        cv.put(dbController.albergues.TYPE, type);
        cv.put(dbController.albergues.ADDRESS, address);

        long id = db.insert(dbController.albergues.TABLE_NAME, null, cv);
        db.close();
        return id;
    }

    public long insertLocality(String title, double lat, double lng) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dbController.locality.TITLE, title);
        cv.put(dbController.locality.LAT, lat);
        cv.put(dbController.locality.LNG, lng);

        long id = db.insert(dbController.locality.TABLE_NAME, null, cv);
        db.close();
        return id;
    }

    public long insertFeedback(String text, double lat, double lng, int status) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dbController.feedback.TEXT, text);
        cv.put(dbController.feedback.LAT, lat);
        cv.put(dbController.feedback.LNG, lng);
        cv.put(dbController.feedback.STATUS, status);

        long id = db.insert(dbController.feedback.TABLE_NAME, null, cv);
        db.close();
        return id;
    }
    public int updateFeedback(long id, int status) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dbController.feedback.STATUS, status);

        int count = db.update(dbController.feedback.TABLE_NAME, cv, dbController.feedback.UID + " = ?", new String[] {id+""});
        db.close();
        return count;
    }

    public int eraseLocality() {
        SQLiteDatabase db = dbController.getWritableDatabase();
        int count = db.delete(dbController.locality.TABLE_NAME, null, null);
        db.close();
        return count;
    }

    public int eraseAlbergues() {
        SQLiteDatabase db = dbController.getWritableDatabase();
        int count = db.delete(dbController.albergues.TABLE_NAME, null, null);
        db.close();
        return count;
    }

    public ArrayList<FeedbackObject> getSavedFeedback(int status) {
        ArrayList<FeedbackObject> list = new ArrayList<>();
        SQLiteDatabase db = dbController.getWritableDatabase();

        String[] columns = {dbController.feedback.UID, dbController.feedback.TEXT, dbController.feedback.LAT, dbController.feedback.LNG};
        Cursor cursor;
        cursor = db.query(dbController.feedback.TABLE_NAME, columns, dbController.feedback.STATUS + " = '"+status+"'", null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(new FeedbackObject(cursor.getLong(cursor.getColumnIndex(dbController.feedback.UID)),
                    cursor.getString(cursor.getColumnIndex(dbController.feedback.TEXT)),
                    cursor.getDouble(cursor.getColumnIndex(dbController.feedback.LAT)),
                    cursor.getDouble(cursor.getColumnIndex(dbController.feedback.LNG))));
        }
        cursor.close();
        return list;
    }

    public Integer getStageFromLocation(Location location) {
        int stage_id = 0;
        SQLiteDatabase db = dbController.getWritableDatabase();
        String[] columns = {dbController.albergues.STAGE};
        Cursor cursor;
        cursor = db.query(dbController.albergues.TABLE_NAME, columns, dbController.albergues.LAT + " = '"+location.getLatitude()+"' AND " +dbController.albergues.LNG + " = '"+location.getLongitude()+"'", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(dbController.albergues.STAGE);
            if (cursor.isNull(index)) {
                stage_id = 0;
            } else {
                stage_id = cursor.getInt(index);
            }
        }
        cursor.close();
        return stage_id;
    }

    public JSONArray getAlbergues(int stage_id) throws JSONException {
        int title, tel, beds, lat, lng, loc, stage, type, address;
        SQLiteDatabase db = dbController.getWritableDatabase();
        String[] columns = {dbController.albergues.TITLE, dbController.albergues.TEL, dbController.albergues.BEDS, dbController.albergues.LAT,
                dbController.albergues.LNG, dbController.albergues.LOCALITY, dbController.albergues.STAGE, dbController.albergues.TYPE, dbController.albergues.ADDRESS};
        Cursor cursor;
        if (stage_id != 0) {
            cursor = db.query(dbController.albergues.TABLE_NAME, columns, dbController.albergues.STAGE + " = '"+stage_id+"'", null, null, null, null);
        } else {
            cursor = db.query(dbController.albergues.TABLE_NAME, columns, null, null, null, null, null);
        }
        JSONArray array = new JSONArray();
        while (cursor.moveToNext()) {
            title = cursor.getColumnIndex(dbController.albergues.TITLE);
            tel = cursor.getColumnIndex(dbController.albergues.TEL);
            beds = cursor.getColumnIndex(dbController.albergues.BEDS);
            lat = cursor.getColumnIndex(dbController.albergues.LAT);
            lng = cursor.getColumnIndex(dbController.albergues.LNG);
            loc = cursor.getColumnIndex(dbController.albergues.LOCALITY);
            stage = cursor.getColumnIndex(dbController.albergues.STAGE);
            type = cursor.getColumnIndex(dbController.albergues.TYPE);
            address = cursor.getColumnIndex(dbController.albergues.ADDRESS);

            array.put(new JSONObject("{\""+dbController.albergues.TITLE+"\": \""+cursor.getString(title)+"\"," +
                    "\""+dbController.albergues.TEL+"\": \""+cursor.getString(tel)+"\"," +
                    "\""+dbController.albergues.BEDS+"\": \""+cursor.getString(beds)+"\"," +
                    "\""+dbController.albergues.LNG+"\": \""+cursor.getDouble(lng)+"\"," +
                    "\""+dbController.albergues.LOCALITY+"\": \""+cursor.getString(loc)+"\"," +
                    "\""+dbController.albergues.STAGE+"\": \""+cursor.getInt(stage)+"\"," +
                    "\""+dbController.albergues.TYPE+"\": \""+cursor.getString(type)+"\"," +
                    "\""+dbController.albergues.ADDRESS+"\": \""+cursor.getString(address)+"\"," +
                    "\""+dbController.albergues.LAT+"\": \""+cursor.getDouble(lat)+"\"}"));
        }
        db.close();
        return array;
    }
    public JSONArray getLocalities() throws JSONException {
        int title, lat, lng;
        SQLiteDatabase db = dbController.getWritableDatabase();
        String[] columns = {dbController.locality.TITLE, dbController.locality.LAT, dbController.locality.LNG};
        Cursor cursor = db.query(dbController.locality.TABLE_NAME, columns, null, null, null, null, null);
        JSONArray array = new JSONArray();
        while (cursor.moveToNext()) {
            title = cursor.getColumnIndex(dbController.locality.TITLE);
            lat = cursor.getColumnIndex(dbController.locality.LAT);
            lng = cursor.getColumnIndex(dbController.locality.LNG);

            array.put(new JSONObject("{\""+dbController.locality.TITLE+"\": \""+cursor.getString(title)+"\"," +
                    "\""+dbController.locality.LNG+"\": \""+cursor.getDouble(lng)+"\"," +
                    "\""+dbController.locality.LAT+"\": \""+cursor.getDouble(lat)+"\"}"));
        }
        db.close();
        return array;
    }

    public void checkVersion() {
        SQLiteDatabase db = dbController.getWritableDatabase();
        db.close();
    }

    public void closeConnection() {
        dbController.close();
    }

    static class DBController extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "caminodatabase";
        private static final int DATABASE_VERSION = 11;
        private final Albergues albergues = new Albergues();
        private final Locality locality = new Locality();
        private final Feedback feedback = new Feedback();
        Context context;


        final class Albergues {
            String TABLE_NAME = "ALBERGUE";
            String UID = "_id";
            String TITLE = "title";
            String TYPE = "type";
            String LAT = "lat";
            String LNG = "lng";
            String TEL = "tel";
            String BEDS = "beds";
            String STAGE = "stage";
            String LOCALITY = "locality";
            String ADDRESS = "address";
        }

        final class Locality {
            String TABLE_NAME = "LOCALITY";
            String UID = "_id";
            String TITLE = "title";
            String LAT = "lat";
            String LNG = "lng";
        }

        final class Feedback {
            String TABLE_NAME = "FEEDBACK";
            String UID = "_id";
            String TEXT = "text";
            String LAT = "lat";
            String LNG = "lng";
            String STATUS = "status";
        }

        public DBController(Context applicationcontext) {
            super(applicationcontext, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = applicationcontext;
        }

        //Creates Table
        @Override
        public void onCreate(SQLiteDatabase database) {
            String query;
            query = "CREATE TABLE " + albergues.TABLE_NAME + " ( " + albergues.UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " " + albergues.TITLE + " TEXT, " + albergues.TYPE + " TEXT," + albergues.ADDRESS + " TEXT," +
                    " " + albergues.LOCALITY + " TEXT, " + albergues.BEDS + " VARCHAR(10)," +
                    " " + albergues.LAT + " DOUBLE," + " " + albergues.STAGE + " INTEGER," +
                    " " + albergues.LNG + " DOUBLE, " + albergues.TEL + " TEXT)";
            database.execSQL(query);
            query = "CREATE TABLE " + locality.TABLE_NAME + " ( " + locality.UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " " + locality.TITLE + " TEXT," + " " + locality.LAT + " DOUBLE," +
                    " " + locality.LNG + " DOUBLE)";
            database.execSQL(query);
            query = "CREATE TABLE " + feedback.TABLE_NAME + " ( " + feedback.UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " " + feedback.TEXT + " TEXT," + " " + feedback.LAT + " DOUBLE," +
                    " " + feedback.LNG + " DOUBLE, " +feedback.STATUS + " INTEGER)";
            database.execSQL(query);
//            Toast.makeText(context, "DB Created", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if(sharedPreferences.contains("db_update_date")) {
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.remove("db_update_date");
                e.commit();
            }
            String query;
            query = "DROP TABLE IF EXISTS " + albergues.TABLE_NAME;
            database.execSQL(query);
            query = "DROP TABLE IF EXISTS " + locality.TABLE_NAME;
            database.execSQL(query);
            query = "DROP TABLE IF EXISTS " + feedback.TABLE_NAME;
            database.execSQL(query);
            onCreate(database);
//            Toast.makeText(context, "DB Upgraded", Toast.LENGTH_LONG).show();
        }
    }

}
