package com.claricom.ccid_nfc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.claricom.ccid_nfc.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    public int counter=0;

    private static NfcAdapter mNfcAdapter;
    private static boolean isScanning = false;
    private static Handler scanningHandler = null;
    private static Runnable scanningRunnable = null;

    private static MainActivity mainActivity;

    public MyService(Context applicationContext) {
        super();
    }

    public MyService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mainActivity = MainActivity.instance;

        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
 //        Intent broadcastIntent = new Intent(this, MyServiceBroadcastReceiver.class);
//        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
            Log.i("in timer", "in timer ++++  "+ (counter++));
            scanNFC();
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void scanNFC(){
        if (isScanning == true) {
            stopTagSearch();
            return;
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            onDestroy();
            return;

        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "NFC is disabled.", Toast.LENGTH_LONG).show();
            onDestroy();
            return;
        }

        isScanning = true;
        handleIntent(mainActivity.getIntent());
        stopTagSearch();
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = null;
        Log.e("service action", action);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }

        if (tag != null) {
            byte[] tagId = tag.getId();
            String hexTagId = Utils.convertByteArrayToHexString(tagId, "");
            //String hexTagId = "324D4920"; //my previous id
            //String hexTagId = "8DB78FDA"; //my current id
            Log.e("tagID", hexTagId);
            onDestroy();
            Intent activityIntent = new Intent(getApplicationContext(), TagInfoActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtra(TagInfoActivity.ARG_TAG_ID, hexTagId);
            getApplicationContext().startActivity(activityIntent);
        }
    }

    public void stopTagSearch() {
        if (isScanning == true) {
            isScanning = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
