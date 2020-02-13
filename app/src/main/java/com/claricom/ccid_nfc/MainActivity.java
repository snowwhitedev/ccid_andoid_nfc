package com.claricom.ccid_nfc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.claricom.ccid_nfc.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    NfcAdapter mNfcAdapter;
    boolean isScanning = false;
    Handler scanningHandler = null;
    Runnable scanningRunnable = null;

    private static String selecectedTab="";
    private static BottomNavigationView navView;

    private boolean first = true;

    private static Timer nfcTimer;

    private static SharedPreferences sharedPref;

    private static Intent mServiceIntent;
    private static MyService mMyService;
    Context ctx;
    static MainActivity instance;


    public MainActivity() {
    }

    public Context getCtx(){
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        instance = this;
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);

        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        mMyService = new MyService(getCtx());
        mServiceIntent = new Intent(getCtx(), mMyService.getClass());
        stopService(mServiceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        int serviceMode = parseInt(sharedPref.getString(getString(R.string.switch_name), getString(R.string.service_mode)));
        if(serviceMode == 1){
            if (!isMyServiceRunning(mMyService.getClass())) {
                startService(mServiceIntent);
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }
    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment selectedFragment = null;
        switch (menuItem.getItemId()){
            case R.id.navigation_call:
                //Toast.makeText(MainActivity.this, "This is call.", Toast.LENGTH_LONG).show();
                selecectedTab = "call";
                selectedFragment = new CallFragment();
                return loadFragment(selectedFragment);
            case R.id.navigation_scan:
                //Toast.makeText(MainActivity.this, "This is scan.", Toast.LENGTH_LONG).show();
                selecectedTab = "scan";
                selectedFragment = new ScanFragment(); //new ScanContentFragment("8DB78FDA");//
                loadFragment(selectedFragment);
                //scanNFC();
                return true;
            case R.id.navigation_error:
                selecectedTab = "error";
                selectedFragment = new ErrorFragment();
                //Toast.makeText(MainActivity.this, "This is error.", Toast.LENGTH_LONG).show();
                return loadFragment(selectedFragment);
            case R.id.navigation_info:
                selecectedTab = "info";
                selectedFragment = new InfoFragment();
                //Toast.makeText(MainActivity.this, "This is info.", Toast.LENGTH_LONG).show();
                //scanNFC();
                return loadFragment(selectedFragment);
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(selecectedTab == "scan"){
            handleIntent(intent);
        } else if(selecectedTab == "info"){
            handleInfoIntent(intent);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(first){
            navView.setSelectedItemId(R.id.navigation_scan);
            first = false;
        }

        nfcTimer = new Timer();
        nfcTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        scanNFC();
                    }
                });
            }
        }, 1000, 3000);

    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, mNfcAdapter);
        stopTagSearch();
        nfcTimer.cancel();
        super.onPause();
    }

    public void scanNFC(){
        if (isScanning == true) {
            stopTagSearch();
            return;
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(MainActivity.this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }
        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "NFC is disabled.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        isScanning = true;
        setupForegroundDispatch(MainActivity.this, mNfcAdapter);
        if(selecectedTab == "scan"){
            handleIntent(getIntent());
        } else if(selecectedTab == "info"){
            handleInfoIntent(getIntent());
        }

         runScanningHandler();

    }

    @SuppressLint("WrongConstant")
    public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        if (adapter == null || isScanning == false) {
            //  scanButton.setText(R.string.scan);
            return;
        }

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        if (adapter == null) {
            return;
        }
        adapter.disableForegroundDispatch(activity);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = null;
        Log.e("main action", selecectedTab + ":" + action);
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

            Intent activityIntent = new Intent(this, TagInfoActivity.class);
            activityIntent.putExtra(TagInfoActivity.ARG_TAG_ID, hexTagId);
            this.startActivity(activityIntent);

        }
    }

    private void handleInfoIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = null;
        Log.e("main action", selecectedTab + ":" + action);
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

            Fragment infoContentFragment = new InfoContentFragment(hexTagId);
            if(infoContentFragment == null){
                return;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, infoContentFragment)
                    .commit();

        }
    }

    public void stopTagSearch() {
        if (isScanning == true) {
            isScanning = false;
            // scanButton.setText(R.string.scan);
            stopForegroundDispatch(MainActivity.this, mNfcAdapter);
            if (scanningHandler != null) {
                scanningHandler.removeCallbacks(scanningRunnable);
                scanningRunnable = null;
                scanningHandler = null;
            }
        }
    }

    private  void runScanningHandler() {
        scanningHandler = new Handler();
        scanningRunnable = new Runnable() {
            public void run() {
                stopTagSearch();
            }
        };
        scanningHandler.postDelayed(scanningRunnable, Integer.parseInt(getString(R.string.timeout)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showConfirmPasswordDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showConfirmPasswordDialog() {
        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        final String password = sharedPref.getString(getString(R.string.password), "");
        if (password.equals("")) {
            goSettings();
            return;
        }

        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_ask_password, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Confirm");
        alertDialog.setMessage("Please input password.");

        final EditText editTextPassword = (EditText) view.findViewById(R.id.editText_dialog_password);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputPassword = editTextPassword.getText().toString();
                if (inputPassword.equals(password)) {
                    alertDialog.dismiss();
                    goSettings();
                }
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(view);
        alertDialog.show();
    }

    private void goSettings() {
        Intent activityIntent = new Intent(this, SettingsActivity.class);
        this.startActivity(activityIntent);
    }

}
