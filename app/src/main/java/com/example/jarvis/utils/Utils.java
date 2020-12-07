package com.example.jarvis.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.jarvis.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Utils {
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
            Logger.e("Jarvis", "[ERR] Need to open app to create a Context");
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
}
