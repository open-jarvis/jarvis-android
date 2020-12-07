package com.example.jarvis;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.jarvis.calls.CallManager;
import com.example.jarvis.calls.CallReceiver;
import com.example.jarvis.calls.JarvisCallManager;
import com.example.jarvis.utils.Actions;
import com.example.jarvis.utils.Logger;
import com.example.jarvis.utils.Storage;
import com.example.jarvis.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    public final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ANSWER_PHONE_CALLS,
            // Manifest.permission.BIND_INCALL_SERVICE
    };
    public static EditText ipOrHostname, jarvisToken, preSharedKeyInput;
    private static TextView connectionState;
    private Button connectButton, reconnectButton;
    private JarvisCallManager jarvisCallManager = new JarvisCallManager();
    public BatteryManager batteryManager = null;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            Map<String, String> battery = new HashMap<>();

            battery.put("level", String.valueOf(level));

            if (batteryManager == null) {
                try {
                    batteryManager = (BatteryManager) Storage.getContext().getSystemService(Context.BATTERY_SERVICE);
                } catch (Exception e) {}
            }
            if (batteryManager != null) {
                battery.put("charging", String.valueOf(batteryManager.isCharging()));
            }

            Logger.i("Jarvis", "[BATTERY] " + level + "%");

            Jarvis.sendBatteryState(battery);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.batteryManager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);

        setContentView(R.layout.activity_main);

        // Set Storage objects
        Storage.setNotificationChannel("com.example.jarvis.NOTIFICATION_LISTENER_TEST_" + new Date().getTime());
        Storage.setContext(getApplicationContext());
        Storage.setActivity(this);

        Logger.d("Jarvis", "[FUNC] MainActivity::onCreate");

        ////////////////////////////////////////////////////////////////
        if (Build.VERSION.SDK_INT >= 29) {
            requestRole();
        }
        requestPermissions();
        initUI();
        checkURI();
        registerCallHandler();

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        ////////////////////////////////////////////////////////////////
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Enable background stuff
        /* unregisterReceiver(nReceiver);
        TinyWebServer.stopServer();*/
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void requestRole() {
        return; /*
        RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
        startActivityForResult(intent, 1);*/
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, this.PERMISSIONS, 1);
    }
    private void initUI() {
        ipOrHostname = (EditText) findViewById(R.id.ipOrHostname);
        jarvisToken = (EditText) findViewById(R.id.jarvisToken);
        connectionState = (TextView) findViewById(R.id.connectionState);
        preSharedKeyInput = (EditText) findViewById(R.id.preSharedKeyInput);

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(Actions.onConnectButtonPressed(ipOrHostname, jarvisToken, connectionState, preSharedKeyInput));

        reconnectButton = (Button) findViewById(R.id.reconnectButton);
        reconnectButton.setOnClickListener(Actions.onReconnectButtonPressed());

        if (Jarvis.isConnected()) {
            connectionState.setText("Already connected to Jarvis at " + Jarvis.getHost());
            connectionState.setTextColor(0xFF1DC558);
        }
    }
    private void checkURI() {
        try {
            // Get URI parameters
            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            Logger.i("Jarvis", "IntentUri: " + data.toString());

            for (String q : data.getEncodedQuery().split("&")) {
                if (q.split("=").length == 2) {
                    switch (q.split("=")[0]) {
                        case "host":
                            ipOrHostname.setText(q.split("=")[1]);
                            Logger.i("Jarvis", "Setting hostname '"+ q.split("=")[1] +"'");
                            break;
                        case "psk":
                            preSharedKeyInput.setText(q.split("=")[1]);
                            Logger.i("Jarvis", "Setting psk '"+ q.split("=")[1] +"'");
                            break;
                        case "token":
                            jarvisToken.setText(q.split("=")[1]);
                            Logger.i("Jarvis", "Setting token '"+ q.split("=")[1] +"'");
                            break;
                        default:
                            continue;
                    }
                }
            }
        } catch (Exception e) {
            Logger.e("Jarvis", "[URI] Error: " + Log.getStackTraceString(e));
        }
    }
    private void registerCallHandler() {
        CallReceiver.callManager = this.jarvisCallManager;
        TelecomManager tm = (TelecomManager) this.getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
        if (tm == null) {
            Logger.w("Jarvis", "[CALL] TelecomManager is null");
            return;
        }
        CallReceiver.telecomManager = tm;
    }

    public static void setConnectionText(String str) {
        MainActivity.connectionState.setText(str);
    }
    public static void setConnectionColor(int color) {
        MainActivity.connectionState.setTextColor(color);
    }
}
