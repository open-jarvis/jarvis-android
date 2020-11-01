package com.example.jarvis;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jarvis.utils.Actions;
import com.example.jarvis.utils.Storage;
import com.example.jarvis.utils.Utils;

import java.util.Date;

public class MainActivity extends Activity {

    private static TextView txtView;
    private NotificationReceiver nReceiver;
    private Thread httpThread, connectThread;
    private EditText ipOrHostname, jarvisToken;
    private TextView connectionState;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);

        Log.d("Jarvis", "[FUNC] MainActivity::onCreate");

        // Set Storage objects
        Storage.setNotificationChannel("com.example.jarvis.NOTIFICATION_LISTENER_TEST_" + new Date().getTime());
        Storage.setContext(getApplicationContext());
        Storage.setActivity(this);

        txtView = (TextView) findViewById(R.id.toolbarText);
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Storage.getNotificationChannel());
        registerReceiver(nReceiver,filter);

        ipOrHostname = (EditText) findViewById(R.id.ipOrHostname);
        jarvisToken = (EditText) findViewById(R.id.jarvisToken);
        connectionState = (TextView) findViewById(R.id.connectionState);

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(Actions.onConnectButtonPressed(ipOrHostname, jarvisToken, connectionState));


        // Find IP Address and show
        Utils.showIPAddress(findViewById(R.id.ipContainer));

        // Launch Threads
        httpThread = Utils.startHttpThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("Jarvis", "[FUNC] MainActivity::onDestroy");

        // Enable background stuff
        /* unregisterReceiver(nReceiver);
        TinyWebServer.stopServer();*/
    }

    public static void addToLog(String message) {
        Log.d("Jarvis", "[FUNC] MainActivity::addToLog");
        txtView.setText(txtView.getText() + "\n" + message);
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Jarvis", "[FUNC] MainActivity.NotificationReceiver::onReceive - " + intent.getStringExtra("notification_event"));
            // String temp = intent.getStringExtra("notification_event") + "\n" + txtView.getText();
            // txtView.setText(temp);
        }
    }
}
