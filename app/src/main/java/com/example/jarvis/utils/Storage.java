package com.example.jarvis.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Storage {
    private static Context c;
    private static String n;
    private static Set<Map<String, String>> notifs;
    private static int notificationId;
    private static Activity activity;

    public static void setContext(Context context) {
        c = context;
    }
    public static Context getContext() {
        return c;
    }

    public static void setNotificationChannel(String channel) {
        Log.d("Jarvis", "[FUNC] Storage::setNotificationChannel - " + channel);
        n = channel;
    }
    public static String getNotificationChannel() {
        Log.d("Jarvis", "[FUNC] Storage::getNotificationChannel - " + n);
        return n;
    }
    public static String getNotificationServiceChannel() {
        Log.d("Jarvis", "[FUNC] Storage::getNotificationServiceChannel - " + n + "_SERVICE");
        return n + "_SERVICE";
    }

    public static void setNotifications(Set<Map<String, String>> notifications) {
        Log.d("Jarvis", "[FUNC] Storage::getNotificationServiceChannel - " + n + "_SERVICE");

        notifs = notifications;
    }
    public static Set<Map<String, String>> getNotifications() {
        Log.d("Jarvis", "[FUNC] Storage::getNotificationServiceChannel");
        return notifs;
    }

    public static int getNotificationId() {
        return notificationId;
    }

    public static void setNotificationId(int notificationId) {
        Storage.notificationId = notificationId;
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        Storage.activity = activity;
    }
}
