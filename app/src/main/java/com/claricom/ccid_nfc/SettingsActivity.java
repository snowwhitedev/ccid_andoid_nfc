package com.claricom.ccid_nfc;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.claricom.ccid_nfc.utils.HttpUtils;
import com.claricom.ccid_nfc.utils.HttpUtils;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

import static java.lang.Integer.parseInt;


public class SettingsActivity extends AppCompatActivity {

    public static final String TAG = "SettingsActivity";
    public static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 99;
    private LinearLayout postJsonContainer;
    private EditText editTextStaffID;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

       androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tag_info);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTextStaffID = findViewById(R.id.edit_staff_id);
        // Show the Up button in the action bar.


        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        // Alert Server
        final EditText editTextServerAddress = (EditText) findViewById(R.id.edit_server_alert_url);
        String serverUrl = sharedPref.getString(getString(R.string.alert_server), getString(R.string.server_alert_url));
        editTextServerAddress.setText(serverUrl);
        editTextServerAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                editor.putString(getString(R.string.alert_server), c.toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }
        });

        //postJson
        postJsonContainer = (LinearLayout) findViewById(R.id.linearLayout_post_json_container);
        final EditText editTextPostJson = (EditText) findViewById(R.id.editText_post_json);
        String postJson = sharedPref.getString(getString(R.string.json), "");
        editTextPostJson.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                editor.putString(getString(R.string.json), c.toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }
        });

        // Password
        final EditText editTextPassword = (EditText) findViewById(R.id.editText_password);
        String password = sharedPref.getString(getString(R.string.password), "");
        editTextPassword.setText(password);
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                editor.putString(getString(R.string.password), c.toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }
        });

        // Method
        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spinner_method);
        spinner.setItems(getString(R.string.method_get), getString(R.string.method_post));
        String method = sharedPref.getString(getString(R.string.method), getString(R.string.method_get));

        int selectedIndex = 0;
        if (method.equals(getString(R.string.method_post))) {
            selectedIndex = 1;
            postJsonContainer.setVisibility(View.VISIBLE);
            editTextPostJson.setText(postJson);
        } else {
            postJsonContainer.setVisibility(View.GONE);
            editTextPostJson.setText("");
        }
        spinner.setSelectedIndex(selectedIndex);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                editor.putString(getString(R.string.method), item);
                if (item.equals(getString(R.string.method_post))) {
                    postJsonContainer.setVisibility(View.VISIBLE);
                } else {
                    postJsonContainer.setVisibility(View.GONE);
                    editTextPostJson.setText("");
                    editor.putString(getString(R.string.json), "");
                }
                editor.commit();
            }
        });

        // Staff ID
        String staffID = sharedPref.getString(getString(R.string.staff_id), getString(R.string.staff_id_code));
        editTextStaffID.setText(staffID);
        editTextStaffID.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                editor.putString(getString(R.string.staff_id), c.toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
            }
        });

        // get Random code
        ImageView randomButton = findViewById(R.id.btn_random_id);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXY0123456789";
                StringBuilder sb = new StringBuilder(8);

                for (int i = 0; i < 8; i++) {

                    int index = (int)(AlphaNumericString.length() * Math.random());
                    sb.append(AlphaNumericString.charAt(index));
                }
                editTextStaffID.setText(sb.toString());

            }
        });

        //get Mac Address
        final ImageView macButton = findViewById(R.id.btn_mac_address);
        macButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(SettingsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    List<ScanResult> scanResults = wifiManager.getScanResults();
//                    Object[] scanArr = scanResults.toArray();
//                    Log.e("ScanString", scanArr.toString());
//                    // editTextStaffID.setText(scanArr.toString().trim().toUpperCase());
//                    editTextStaffID.setText(Integer.toString(scanResults.size()));

                }else{
                    // Write you code here if permission already given.
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    String scanString = scanResults.get(0).toString();

                    Log.e("mac", scanString);

                    String[] splitStr = scanString.split("\\s*,\\s*");
                    String macString = splitStr[1];

                    splitStr = macString.split(":");
                    String macAddress = "";
                    for(int i = splitStr.length - 1; i > splitStr.length - 5; i--){
                        macAddress += splitStr[i].trim();
                    }
                    editTextStaffID.setText(macAddress.trim().toUpperCase());
                }
            }

        });

        Button btn_add_inventory = findViewById(R.id.btn_add_inventory);
        btn_add_inventory.setOnClickListener(new View.OnClickListener() {
            String req_url;
            String selMethod = sharedPref.getString(getString(R.string.method), getString(R.string.method_get));
            String selStaffID = sharedPref.getString(getString(R.string.staff_id), getString(R.string.staff_id_code));
            String selServerUrl = sharedPref.getString(getString(R.string.alert_server), getString(R.string.server_alert_url));

            @Override
            public void onClick(View view) {
                if(selMethod.equals("GET")){
                    RequestParams rp = new RequestParams();
                    rp.put("cardId", selStaffID);
                    HttpUtils.get(selServerUrl, rp, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            // If the response is JSONObject instead of expected JSONArray
                            Log.d(TAG, "success response : " + response);
                            processSuccessResponse(response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.d(TAG, "failure response : " + responseString);
                            processFailureResponse();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d(TAG, "failure response...");
                            processFailureResponse();
                        }
                    });
                } else {
                    JSONObject jsonObject = null;

                    if (jsonObject == null) {
                        jsonObject = new JSONObject();
                    }
                    try {
                        jsonObject.put("cardId", selStaffID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String body = jsonObject.toString();
                    HttpUtils.post(getApplicationContext(), selServerUrl, body, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            // If the response is JSONObject instead of expected JSONArray
                            processSuccessResponse(response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            processFailureResponse();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            processFailureResponse();
                        }
                    });
                }

            }
        });

        int val = parseInt(sharedPref.getString(getString(R.string.switch_name), getString(R.string.service_mode)));
        boolean serviceMode = false;
        final Switch serviceSwitch = findViewById(R.id.service_switch);
        serviceMode = (val == 0)?false:true;

        serviceSwitch.setChecked(serviceMode);
        serviceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(serviceSwitch.isChecked()){
                    editor.putString(getString(R.string.switch_name), new Integer(1).toString());
                    editor.commit();
                } else {
                    editor.putString(getString(R.string.switch_name), new Integer(0).toString());
                    editor.commit();
                }

            }
        });
    }

    private void processSuccessResponse (JSONArray response){
        try {
            showAlert("Attention", response.get(0).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processFailureResponse () {
        showAlert("Attention", "Registration Failed");
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
