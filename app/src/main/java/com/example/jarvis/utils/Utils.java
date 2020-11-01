package com.example.jarvis.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.jarvis.MainActivity;
import com.example.jarvis.NotificationService;
import com.example.jarvis.R;
import com.example.jarvis.ext.web.TinyWebServer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class Utils {
    public static void showIPAddress(TextView ipView) {
        Log.d("Jarvis", "[FUNC] Utils::showIPAddress");

        try {
            WifiManager wm = (WifiManager) Storage.getContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            ipView.setText("IP Address: " + ip);
        } catch (NullPointerException e) {
            ipView.setText("IP Address: unknown");
        }
    }

    public static String replaceGroup(String regex, String source, int groupToReplace, String replacement) {
        return replaceGroup(regex, source, groupToReplace, 1, replacement);
    }

    public static String replaceGroup(String regex, String source, int groupToReplace, int groupOccurrence, String replacement) {
        Matcher m = Pattern.compile(regex).matcher(source);
        for (int i = 0; i < groupOccurrence; i++)
            if (!m.find()) return source; // pattern not met, may also throw an exception here
        return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), replacement).toString();
    }

    public static int sendNotification(String title, String content, Boolean uncancellable, int notifyID) {
        String CHANNEL_ID = "jarvis_channel_01";
        CharSequence name = "Jarvis";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        try {
            // Create notification
            Notification notification = new Notification.Builder(Storage.getContext())
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(uncancellable)
                    .build();

            // Send notification
            NotificationManager nManager = (NotificationManager) Storage.getContext().getSystemService(NOTIFICATION_SERVICE);
            nManager.createNotificationChannel(mChannel);
            nManager.notify(notifyID, notification);
            return notifyID;
        } catch (NullPointerException e) {
            Log.e("Jarvis", "[ERR] Need to open app to create a Context");
            return -1;
        }
    }
    public static int sendNotification(String title, String content, Boolean uncancellable) {
        return sendNotification(title, content, uncancellable, 1);
    }
    public static int updateNotification(int id, String title, String content, Boolean uncancellable) {
        return sendNotification(title, content, uncancellable, id);
    }

    public static void hideKeyboard() {
        View view = Storage.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) Storage.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static Thread startHttpThread() {
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TinyWebServer.startServer("0.0.0.0",9000, Storage.getContext().getApplicationInfo().dataDir);
                } catch (Exception e) {
                    Log.e("Jarvis", "[HTTP] Error in httpThread: " + Log.getStackTraceString(e));
                }
            }
        });
        httpThread.start();
        return httpThread;
    }
}
