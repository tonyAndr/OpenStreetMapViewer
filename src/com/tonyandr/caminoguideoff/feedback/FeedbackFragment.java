package com.tonyandr.caminoguideoff.feedback;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tonyandr.caminoguideoff.R;
import com.tonyandr.caminoguideoff.constants.AppConstants;

import org.apache.http.Header;

public class FeedbackFragment extends Fragment implements AppConstants {

    private Button sendFeedbackBtn;
    private Button okFeedbackBtn;
    private Button retryFeedbackBtn;
    private Button cancelFeedbackBtn;
    private EditText editText;
    private SharedPreferences mPrefs;
    private RelativeLayout successLayout;
    private LinearLayout mainLayout;
    private LinearLayout formLayout;
    private LinearLayout progressLayout;
    private LinearLayout resultLayout;
    private InputMethodManager imm;

    private final static int STATUS_OK = 0;
    private final static int STATUS_ERROR = 1;
    private final static int STATUS_NOCONNECTION = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_feedback, container, false);
        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mainLayout = (LinearLayout) v.findViewById(R.id.feedback_linear_layout);
        formLayout = (LinearLayout) v.findViewById(R.id.feedback_form);
        progressLayout = (LinearLayout) v.findViewById(R.id.feedback_inprogress);
        resultLayout = (LinearLayout) v.findViewById(R.id.feedback_result);

        editText = (EditText) v.findViewById(R.id.feedback_text);

        editText.setPadding(8, 8, 8, 8); // bug -> doesn't work in xml
        imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

//        checkBox = (CheckBox) findViewById(R.id.feedback_geo_cb);
//        successLayout = (RelativeLayout) v.findViewById(R.id.feedback_success_layout);

        retryFeedbackBtn = (Button) v.findViewById(R.id.feedback_result_btn_RETRY);
        okFeedbackBtn = (Button) v.findViewById(R.id.feedback_result_btn_OK);
        cancelFeedbackBtn = (Button) v.findViewById(R.id.feedback_result_btn_CANCEL);

        sendFeedbackBtn = (Button) v.findViewById(R.id.send_feedback_btn);
        sendFeedbackBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        sendFeedbackBtnAction();

                        break;
                    default:
                        break;
                }
                return false;

            }
        });

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                onBackgroundClick();
            }
        });


        return v;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showFragment();
    }

    private void showFragment() {
        mainLayout.setAlpha(0);
        mainLayout.animate().alpha(1.0f).setDuration(400).withEndAction(new Runnable() {
            @Override
            public void run() {
                formLayout.setVisibility(View.VISIBLE);
                formLayout.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        formLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
                    }
                });
            }
        });
    }

    private void onBackgroundClick() {
        if (editText.getText().length() > 0) {
            saveText();
        }
        getActivity().onBackPressed();
    }

    private void saveText() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("feedback_text", editText.getText().toString());
        editor.commit();
    }

    private void removeText() {
        editText.setText("");
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("feedback_text", "");
            editor.commit();
    }

    private void loadText() {
        editText.setText(mPrefs.getString("feedback_text", ""));
    }

    @Override
    public void onPause() {
        super.onPause();
        saveText();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadText();
    }

    //
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDestroyViewAnimation();
    }

    public void onDestroyViewAnimation() {
        formLayout.animate().scaleX(0).scaleY(0).alpha(0).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                formLayout.setVisibility(View.GONE);
            }
        });
    }


    private void sendFeedbackBtnAction() {
//        sendFeedbackBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        if (editText.getText().length() < 10) {
            Toast.makeText(getActivity(), "Write minimum 10 symbols", Toast.LENGTH_SHORT).show();
        } else {
            double lat = 0, lng = 0;
            if (mPrefs != null) {
                String[] loc_string = mPrefs.getString("location-string", "").split(",");
                if (loc_string.length > 1) {
                    lat = Double.parseDouble(loc_string[0]);
                    lng = Double.parseDouble(loc_string[1]);
                }
            }
            if (isNetworkAvailable()) {
                final RequestParams requestParams = new RequestParams();
                requestParams.add("text", editText.getText().toString());
                requestParams.add("lat", "" + lat);
                requestParams.add("lng", "" + lng);
                final double f_lat = lat, f_lng = lng;
                Log.w(DEBUGTAG, "http://alberguenajera.es/projects/gms/feedback_handler.php?text=" + editText.getText().toString() + "&lat=" + lat + "&lng=" + lng);
                AsyncHttpClient client = new AsyncHttpClient();
                client.get("http://alberguenajera.es/projects/gms/feedback_handler.php", requestParams, new BaseJsonHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        formLayout.setVisibility(View.GONE);
                        progressLayout.setVisibility(View.VISIBLE);
                        progressLayout.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                progressLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
                            }
                        });
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                        Log.w(DEBUGTAG, "raw: " + rawJsonResponse);
                        if (rawJsonResponse.equals("OK")) {
//                                Toast.makeText(FeedbackActivity.this, "Thank you for feedback! :)", Toast.LENGTH_SHORT).show();
                            removeText();
                            showResult(STATUS_OK);
                        } else {
                            showResult(STATUS_ERROR);
                            Log.e(DEBUGTAG, "Response NOT OK: " + rawJsonResponse);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                        showResult(STATUS_ERROR);
                        Log.e(DEBUGTAG, "Throwable: " + throwable.toString());
                        Log.e(DEBUGTAG, "Response: " + rawJsonData);
                        Toast.makeText(getActivity(), "Failed to send, message saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                        return null;
                    }
                });
            } else {
                showResult(STATUS_NOCONNECTION);
                Toast.makeText(getActivity(), "No connection, message saved", Toast.LENGTH_SHORT).show();
            }
        }

//            }
//        });
    }

    private void showResult(int status) {
        switch (status) {
            case STATUS_OK:
                progressLayout.setVisibility(View.GONE);
                resultLayout.setVisibility(View.VISIBLE);
                ((TextView) resultLayout.findViewById(R.id.feedback_result_text)).setText(getString(R.string.feedback_thank_you));
                ((ImageView) resultLayout.findViewById(R.id.feedback_result_img)).setVisibility(View.GONE);
                resultLayout.findViewById(R.id.feedback_result_btn_layout).setVisibility(View.GONE);
                okFeedbackBtn.setVisibility(View.VISIBLE);

                resultLayout.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        resultLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
                    }
                });
                okFeedbackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                        resultLayout.setVisibility(View.GONE);

                    }
                });
                break;
            case STATUS_ERROR:
                progressLayout.setVisibility(View.GONE);
                ((TextView) resultLayout.findViewById(R.id.feedback_result_text)).setText(getString(R.string.feedback_error));
                ((ImageView) resultLayout.findViewById(R.id.feedback_result_img)).setVisibility(View.VISIBLE);
                ((ImageView) resultLayout.findViewById(R.id.feedback_result_img)).setImageResource(R.drawable.wrong);
                resultLayout.findViewById(R.id.feedback_result_btn_layout).setVisibility(View.VISIBLE);
                okFeedbackBtn.setVisibility(View.GONE);
                resultLayout.setVisibility(View.VISIBLE);
                resultLayout.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        resultLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
                    }
                });
                cancelFeedbackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveText();
                        getActivity().onBackPressed();
                        resultLayout.setVisibility(View.GONE);
                    }
                });
                retryFeedbackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Retry
                        resultLayout.setVisibility(View.GONE);
                        sendFeedbackBtnAction();
                    }
                });
                break;
            case STATUS_NOCONNECTION:
                formLayout.setVisibility(View.GONE);
                resultLayout.setVisibility(View.VISIBLE);
                ((TextView) resultLayout.findViewById(R.id.feedback_result_text)).setText(getString(R.string.feedback_noconnection));
                ((ImageView) resultLayout.findViewById(R.id.feedback_result_img)).setVisibility(View.VISIBLE);
                ((ImageView) resultLayout.findViewById(R.id.feedback_result_img)).setImageResource(R.drawable.wrong);
                resultLayout.findViewById(R.id.feedback_result_btn_layout).setVisibility(View.VISIBLE);
                okFeedbackBtn.setVisibility(View.GONE);
                resultLayout.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        resultLayout.animate().scaleX(1f).scaleY(1f).setDuration(200);
                    }
                });
                cancelFeedbackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveText();
                        closeResultLayoutEffect();
                        getActivity().onBackPressed();

                    }
                });
                retryFeedbackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Retry
                        closeResultLayoutEffect();
                        sendFeedbackBtnAction();
                    }
                });
        }
    }

    private void closeResultLayoutEffect() {
        resultLayout.animate().scaleX(0.7f).scaleY(0.7f).alpha(0).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                resultLayout.setVisibility(View.GONE);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
