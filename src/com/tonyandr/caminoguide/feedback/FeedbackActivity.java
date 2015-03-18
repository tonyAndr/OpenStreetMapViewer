package com.tonyandr.caminoguide.feedback;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tonyandr.caminoguide.NavigationDrawerLayout;
import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.utils.DBControllerAdapter;
import com.tonyandr.caminoguide.utils.FeedbackObject;
import com.tonyandr.caminoguide.utils.HttpPostClient;

import org.apache.http.Header;

import java.util.ArrayList;

public class FeedbackActivity extends ActionBarActivity implements AppConstants{

    private Toolbar toolbar;
    private NavigationDrawerLayout drawerFragment;
    private Button sendFeedbackBtn;
    private Button okFeedbackBtn;
    private DBControllerAdapter dbController;
    private EditText editText;
    private CheckBox checkBox;
    private SharedPreferences mPrefs;
    private RelativeLayout successLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        dbController = DBControllerAdapter.getInstance(this);
        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        editText = (EditText) findViewById(R.id.feedback_text);
        checkBox = (CheckBox) findViewById(R.id.feedback_geo_cb);
        successLayout = (RelativeLayout) findViewById(R.id.feedback_success_layout);

        okFeedbackBtn = (Button) findViewById(R.id.feedback_success_OK);
        okFeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                successLayout.setVisibility(View.GONE);
            }
        });


        sendFeedbackBtn = (Button) findViewById(R.id.send_feedback_btn);
        sendFeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() < 10) {
                    Toast.makeText(FeedbackActivity.this, "Write minimum 10 symbols", Toast.LENGTH_SHORT).show();
                } else {
                    double lat=0, lng=0;
                    if (mPrefs != null) {
                        String[] loc_string = mPrefs.getString("location-string", "").split(",");
                        if (loc_string.length > 1) {
                            lat = Double.parseDouble(loc_string[0]);
                            lng = Double.parseDouble(loc_string[1]);
                        }
                    }
                    if (isNetworkAvailable()) {
                        sendSavedFeedback();
                        final RequestParams requestParams = new RequestParams();
                        requestParams.add("text", editText.getText().toString());
                        requestParams.add("lat", ""+lat);
                        requestParams.add("lng", ""+lng);
                        final double f_lat = lat, f_lng = lng;
                        HttpPostClient.post("", requestParams, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                Log.e(DEBUGTAG, "Throwable: " + throwable.toString());
//                                Log.e(DEBUGTAG, "Response: " + responseString);
                                dbController.insertFeedback(editText.getText().toString(), f_lat, f_lng, 0);
                                Toast.makeText(FeedbackActivity.this, "Failed to send, message saved", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                if (responseString.equals("OK")) {
//                                    dbController.updateFeedback(id, 1);
                                    dbController.insertFeedback(editText.getText().toString(), f_lat, f_lng, 1);
//                                Toast.makeText(FeedbackActivity.this, "Thank you for feedback! :)", Toast.LENGTH_SHORT).show();
                                    successLayout.setVisibility(View.VISIBLE);
                                } else {
//                                    Log.e(DEBUGTAG, "Response NOT OK: " + responseString);
                                }
                            }
                        });
                    } else {
                        dbController.insertFeedback(editText.getText().toString(), lat, lng, 0);
                        Toast.makeText(FeedbackActivity.this, "No connection, message saved", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (NavigationDrawerLayout) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_drawer);
        drawerFragment.setUp(R.id.fragment_nav_drawer,(DrawerLayout)findViewById(R.id.drawer_layout), toolbar);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendSavedFeedback() {
        ArrayList<FeedbackObject> feedbackObjects = new ArrayList<>();
        feedbackObjects = dbController.getSavedFeedback(FEEDBACK_STATUS_WAIT);
        if (feedbackObjects.size() > 0) {
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
//                        Log.e(DEBUGTAG, "Throwable: " + throwable.toString());
//                        Log.e(DEBUGTAG, "Response: " + responseString);
//                            Toast.makeText(SplashActivity.this, "Failed to send, message saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        if (responseString.equals("OK")) {
                            dbController.updateFeedback(_id, 1);
//                                Toast.makeText(SplashActivity.this, "Thank you for feedback! :)", Toast.LENGTH_SHORT).show();
//                            Log.w(DEBUGTAG, "Feedback sent, id: " + _id);
                        } else {
//                            Log.e(DEBUGTAG, "Response NOT OK: " + responseString);
                        }
                    }
                });
            }
        }

    }
}
