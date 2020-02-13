package com.claricom.ccid_nfc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.claricom.ccid_nfc.models.TagInfoItem;
import com.claricom.ccid_nfc.utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class TagInfoActivity extends AppCompatActivity {

    public static final String TAG = "TagInfoActivity";
    public static final String ARG_TAG_ID = "tag_id";

    private SharedPreferences sharedPref;
    private String serverUrl;
    private String method;

    private static TextView msgText;
    private static TextView alarmText;
    private static TextView facilityText;
    private static TextView floorText;
    private static TextView departText;
    private static TextView roomText;
    private static TextView bedText;
    private static TextView patientText;
    private static ImageView roomImg;

    private static LinearLayout detailLayout;
    private static ScrollView patientsScroll;
    private static HorizontalScrollView pImgHScroll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tag_info);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        msgText = findViewById(R.id.res_msg);
        alarmText = findViewById(R.id.res_alarm);
        facilityText = findViewById(R.id.res_facility);
        floorText = findViewById(R.id.res_floor);
        departText = findViewById(R.id.res_depart);
        roomText = findViewById(R.id.res_room);
        bedText = findViewById(R.id.res_bed);
        roomImg = findViewById(R.id.room_img);
        patientText = findViewById(R.id.res_patient);

        detailLayout = (LinearLayout) findViewById(R.id.detail_layout);
        patientsScroll = (ScrollView) findViewById(R.id.scrollView1);
        pImgHScroll = (HorizontalScrollView)findViewById(R.id.hscrollView1);

        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        serverUrl = sharedPref.getString(getString(R.string.ems_server), getString(R.string.server_nfc_url));

        method = sharedPref.getString(getString(R.string.method), getString(R.string.method_get));
//        method = "GET";
        String tagID =  getIntent().getExtras().getString(ARG_TAG_ID);
        if (tagID != null) {
            showProgress(true);
            getInfoFromTag(tagID);
        }
    }

    private void showProgress(boolean show) {
        if(show){
            roomImg.setImageResource(R.drawable.loading);
            Glide.with(this).load("file:///android_asset/loading.gif").into(roomImg);
            detailLayout.setVisibility(View.INVISIBLE);
            patientsScroll.setVisibility(View.INVISIBLE);
            pImgHScroll.setVisibility(View.INVISIBLE);
        } else {
            detailLayout.setVisibility(View.VISIBLE);
            patientsScroll.setVisibility(View.VISIBLE);
            pImgHScroll.setVisibility(View.VISIBLE);
        }
    }

    private void getInfoFromTag(String tagID) {
        if (method.equals(getString(R.string.method_get))) {
            RequestParams rp = new RequestParams();
            rp.put("nfcTag", tagID);
            HttpUtils.get(serverUrl, rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.d(TAG, "success response : " + response);
                    showProgress(false);
                    processSuccessResponse(response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "failure response : " + responseString);
                    showProgress(false);
                    processFailureResponse();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, "failure response...");
                    showProgress(false);
                    processFailureResponse();
                }
            });

        } else if (method.equals(getString(R.string.method_post))) {
            String json = sharedPref.getString(getString(R.string.json), "");
            JSONObject jsonObject = null;
            if (!json.equals("")) {
                try {
                    jsonObject = new JSONObject(json);

                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                    jsonObject = null;
                }
            }

            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
            try {
                jsonObject.put("nfcTag", tagID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String body = jsonObject.toString();
            HttpUtils.post(this, serverUrl, body, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.d(TAG, "success response : " + response);
                    showProgress(false);
                    processSuccessResponse(response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "failure response : " + responseString);
                    showProgress(false);
                    processFailureResponse();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, "failure response...");
                    showProgress(false);
                    processFailureResponse();
                }
            });
        }
    }

    private void processSuccessResponse (JSONObject response) {
        try {
            if (response.getInt("Code") == 500) {
                showAlert("Attention", "Device not found. Please try another one.");
                return;
            } else if (response.getInt("Code") == 403) {
                showAlert("Attention", response.getString("Message"));
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<TagInfoItem> tagInfoItems = new ArrayList<TagInfoItem>();
        Iterator<String> iterator = response.keys();
        String keys = "";
        String vals = "";
        while (iterator.hasNext()) {
            String key = iterator.next();

            try {
                String value = response.getString(key);
                tagInfoItems.add(new TagInfoItem(key, value));
                SharedPreferences.Editor editor = sharedPref.edit();
                switch (key){
                    case "Message":
                        msgText.setText("Message " + value);
                        editor.putString(getString(R.string.msgText_val), value);
                        editor.commit();break;
                    case "LastAlarm":
                        alarmText.setText("Alarm:" + value);
                        editor.putString(getString(R.string.alarmText_val), value);
                        editor.commit();break;
                    case "Facility":
                        facilityText.setText("Facility: " + value);
                        editor.putString(getString(R.string.facilityText_val), value);
                        editor.commit();break;
                    case "Floor":
                        floorText.setText("Floor: " + value);
                        editor.putString(getString(R.string.floorText_val), value);
                        editor.commit();break;
                    case "Department":
                        departText.setText("Department: " + value);
                        editor.putString(getString(R.string.departText_val), value);
                        editor.commit();break;
                    case "Room":
                        roomText.setText("Room: " + value);
                        editor.putString(getString(R.string.roomText_val), value);
                        editor.commit();break;
                    case "Bed":
                        bedText.setText("Bed: " + value);
                        editor.putString(getString(R.string.bedText_val), value);
                        editor.commit();break;
                    case "ImageUrl":
                        editor.putString(getString(R.string.imageUrl_val), value);
                        editor.commit();
                        Log.e("put-------", value);
                        try {
                            URI url = new URI(serverUrl);
                            String host = url.getHost();
                            String imagePath = value.replace("~/","/");
                            String imageUrl = url.getScheme() + "://" + host + imagePath;
                            Picasso.get().load(imageUrl).into(roomImg);
                          // Toast.makeText(this, imageUrl, Toast.LENGTH_LONG).show();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Patient":
                        patientText.setText(value);
                        editor.putString(getString(R.string.patientText_val), value);
                        editor.commit();break;

                }
            } catch (JSONException e) {
                // Something went wrong!
            }
        }
    }

    private void processFailureResponse () {
        showAlert("Attention", "Not Found");
    }

    private void showAlert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                       // finish();
                    }
                });
        alertDialog.show();
    }


}
