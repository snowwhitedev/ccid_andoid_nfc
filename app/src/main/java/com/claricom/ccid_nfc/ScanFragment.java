package com.claricom.ccid_nfc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.claricom.ccid_nfc.utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class ScanFragment extends Fragment {

    private String alertUrl;
    private String method;
    private String staffID;
    private boolean alert_btn_enable = false;
    private static ImageView sendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();

        sendButton = getActivity().findViewById(R.id.btn_send_req);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getActivity().getString(R.string.app_name), Context.MODE_PRIVATE);

        alertUrl = sharedPref.getString(getActivity().getString(R.string.alert_server), getActivity().getString(R.string.server_alert_url));
        method = sharedPref.getString(getActivity().getString(R.string.method), getActivity().getString(R.string.method_get));
        staffID = sharedPref.getString(getActivity().getString(R.string.staff_id), getActivity().getString(R.string.staff_id_code));
        if(alertUrl!=null && method!=null &&staffID!=null){
            sendButton.setImageResource(R.drawable.alert);
            alert_btn_enable = true;
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAlertReq();
            }

        });
    }

    private void sendAlertReq(){
        if(alert_btn_enable){
            if(method.equals("GET")){
                RequestParams rp = new RequestParams();
                rp.put("cardId", staffID);
                Log.e("card", staffID);
                Log.e("url", alertUrl);
                HttpUtils.get(alertUrl, rp, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
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
            } else {
                JSONObject jsonObject = null;

                if (jsonObject == null) {
                    jsonObject = new JSONObject();
                }
                try {
                    jsonObject.put("cardId", staffID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String body = jsonObject.toString();
                Log.e("card", staffID);
                Log.e("url", alertUrl);
                HttpUtils.post(getActivity().getApplicationContext(), alertUrl, body, new JsonHttpResponseHandler() {
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
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
