package com.tonyandr.caminoguideoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tonyandr.caminoguideoff.constants.AppConstants;
import com.tonyandr.caminoguideoff.map.MapActivity;
import com.tonyandr.caminoguideoff.utils.DBControllerAdapter;
import com.tonyandr.caminoguideoff.utils.DBUpdateService;
import com.tonyandr.caminoguideoff.utils.FeedbackObject;
import com.tonyandr.caminoguideoff.utils.HttpPostClient;
import com.tonyandr.caminoguideoff.utils.JsonFilesHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class SplashActivity extends ActionBarActivity implements AppConstants {

    private static int SPLASH_SCREEN_DELAY = 400;
    private boolean mUserLearnedApp;
    private boolean mFromSavedInstanceState;
    private Date mLastDBUpdate;
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;
    private ProgressBar circleProgressBar;
    private TextView textView;
    private TextView textViewInfo;
    private int mAlberguesArrayLength;
    private DBControllerAdapter dbControllerAdapter;
    private SharedPreferences mPrefs;
    private JsonFilesHandler jfh;
    private String mFullAppPath = Environment.getExternalStorageDirectory().getPath() + APP_PATH;

//    private JSONArray albergues;
//    private JSONArray localities;

    public BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            textViewInfo.setText("Loading data...");
            circleProgressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            progressBar.setProgress(intent.getIntExtra("progress", 0));
            textView.setText(intent.getIntExtra("progress", 0) + "%");
            if (intent.getBooleanExtra("finished", false)) {
                startMapActivity();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        nullLocation();
//        new CopyAssetsTask().execute();

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        circleProgressBar = (ProgressBar) findViewById(R.id.progressBar3);
        progressBar.setMax(100);
        textView = (TextView) findViewById(R.id.splash_textview);
        textViewInfo = (TextView) findViewById(R.id.splash_tv_info);

        dbControllerAdapter = DBControllerAdapter.getInstance(this);
        dbControllerAdapter.checkVersion(); // Re-create db if new version

        jfh = new JsonFilesHandler(this);

        IntentFilter filter = new IntentFilter("DBUpdateService");
        registerReceiver(br, filter);

        mUserLearnedApp = Boolean.valueOf(readFromPreferences(KEY_USER_LEARNED_APP, "false"));
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }

        File map_file = new File(mFullAppPath + APP_OFFLINE_MAP_FILE);

        if (!map_file.exists()) {
            new CopyAssetsTask().execute();
        } else {
            updateDataBase();
        }


    }

    private void updateDataBase() {
        if (!mUserLearnedApp && !mFromSavedInstanceState) {
            if (!mUserLearnedApp) {
                mUserLearnedApp = true;
                saveToPreferences(KEY_USER_LEARNED_APP, mUserLearnedApp + "");
            }
            // getfromfile
            String alb_path = "json/albergues.json";
            JSONObject fileObj = jfh.parseJSONObj(alb_path);
            try {
                JSONArray albergues = fileObj.getJSONArray("albergues");
                JSONArray localities = fileObj.getJSONArray("localities");
                mAlberguesArrayLength = albergues.length();
                //update
                Intent intent = new Intent(SplashActivity.this, DBUpdateService.class);
                intent.putExtra("albergues", albergues.toString());
                intent.putExtra("localities", localities.toString());
                startService(intent);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Can't update database!", Toast.LENGTH_LONG).show();
            }

        } else {
            if (isNetworkAvailable()) {
                ArrayList<FeedbackObject> feedbackObjects = new ArrayList<>();
                feedbackObjects = dbControllerAdapter.getSavedFeedback(FEEDBACK_STATUS_WAIT);
                for (FeedbackObject item:feedbackObjects) {
                    RequestParams requestParams = new RequestParams();
                    requestParams.add("text", item.text);
                    requestParams.add("lat", ""+item.lat);
                    requestParams.add("lng", ""+item.lng);
                    final long id = item.id;
                    HttpPostClient.post("", requestParams, new TextHttpResponseHandler() {
                        private long _id;
                        @Override
                        public void onStart() {
                            _id = id;
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                            Log.e(DEBUGTAG, "Feedback send Throwable: " + throwable.toString());
//                            Log.e(DEBUGTAG, "Response: " + responseString);
//                            Toast.makeText(SplashActivity.this, "Failed to send, message saved", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            if (responseString.equals("OK")) {
                                dbControllerAdapter.updateFeedback(_id, 1);
//                                Toast.makeText(SplashActivity.this, "Thank you for feedback! :)", Toast.LENGTH_SHORT).show();
//                                Log.w(DEBUGTAG, "Feedback sent, id: " + _id);
                            } else {
//                                Log.e(DEBUGTAG, "Response NOT OK: " + responseString);
                            }
                        }
                    });
                }

                final AsyncHttpClient client = new AsyncHttpClient();
                client.get("http://alberguenajera.es/projects/gms/get_update_date.php", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        boolean updated = false;
                        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            mLastDBUpdate = df.parse(readFromPreferences("db_update_date", "2010-02-13 15:48:02"));
                            for (int i = 0; i < response.length(); i++) {
                                if (df.parse(response.getString(i)).after(mLastDBUpdate)) {
                                    updated = true;
                                    mLastDBUpdate = df.parse(response.getString(i));
                                }
                            }
                            saveToPreferences("db_update_date", df.format(mLastDBUpdate));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (updated == true) {
                            client.get("http://alberguenajera.es/projects/gms/get_albergues_json.php", new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                    Log.d("dbservice", "success obj");

                                    Intent intent = new Intent(SplashActivity.this, DBUpdateService.class);
                                    try {
                                        JSONArray albergues = response.getJSONArray("albergues");
                                        mAlberguesArrayLength = albergues.length();
                                        JSONArray localities = response.getJSONArray("localities");
                                        intent.putExtra("albergues", albergues.toString());
                                        intent.putExtra("localities", localities.toString());
                                        startService(intent);

                                        if (!mUserLearnedApp) {
                                            mUserLearnedApp = true;
                                            saveToPreferences(KEY_USER_LEARNED_APP, mUserLearnedApp + "");
                                        }
                                        saveToPreferences("db_update_date", df.format(mLastDBUpdate));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(SplashActivity.this, "Error while updating :/", Toast.LENGTH_LONG).show();
                                    }


//                                    try {
//                                        albergues = response.getJSONArray("albergues");
//                                        localities = response.getJSONArray("localities");
//
//
//                                        if (!mUserLearnedApp) {
//                                            mUserLearnedApp = true;
//                                            saveToPreferences(SplashActivity.this, KEY_USER_LEARNED_APP, mUserLearnedApp + "");
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                        Toast.makeText(SplashActivity.this, "Error while updating :/", Toast.LENGTH_LONG).show();
//                                    }


                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                    Log.e(DEBUGTAG, "Get alb Throwable: " + throwable.toString());
                                    startMapActivity();
                                }
                            });
                        } else {
                            startMapActivity();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        Log.e(DEBUGTAG, "get date Throwable: " + throwable.toString());
                        startMapActivity();
                    }
                });
            } else {
                startMapActivity();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void nullLocation() {
        if (mPrefs.contains("location-string")) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.remove("location-string");
            editor.commit();
        }
    }

    private void startMapActivity() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, MapActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, SPLASH_SCREEN_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    public void saveToPreferences(String preferenceName, String preferenceValue) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public String readFromPreferences(String preferenceName, String defaultValue) {
        return mPrefs.getString(preferenceName, defaultValue);
    }


    private boolean copyAssets() {
        boolean success = true;
        AssetManager assetManager = getAssets();
        String[] files = null;
        String output = Environment.getExternalStorageDirectory().getPath() + APP_PATH;
        String folder = "CaminoGuideOffline";
        try {
            files = assetManager.list(folder);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
            success = false;
        }
        File outDir = new File(output);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(folder +"/" +filename);
                File outFile = new File(output, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
                success = false;
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
        return success;
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }


    class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

        public  CopyAssetsTask() {

        }
        private boolean failed = false;

        @Override
        protected void onPreExecute() {
//            Toast.makeText(SplashActivity.this, "Preparing map, please wait...", Toast.LENGTH_LONG).show();
            textViewInfo.setText("Preparing offline map...");
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (copyAssets()) {
                File zip = new File(mFullAppPath + "CaminoGuideOffline.zip");
                try {
                    unzip(zip,new File(mFullAppPath));
                } catch (IOException e) {
                    e.printStackTrace();
                    failed = true;
                }
                if (zip.exists()) {
                    zip.delete();
                }
            } else {
                failed = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!failed) {

            } else {
                Toast.makeText(SplashActivity.this,"Error while unzipping map file!", Toast.LENGTH_LONG).show();
            }
            updateDataBase();
        }
    }

}
