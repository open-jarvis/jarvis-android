package com.example.jarvis.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.Set;

public class Storage {
    private static Context c;
    private static String n;
    private static Activity activity;

    public static void setContext(Context context) {
        c = context;
    }
    public static Context getContext() {
        return c;
    }

    public static void setNotificationChannel(String channel) {
        Logger.d("Jarvis", "[FUNC] Storage::setNotificationChannel - " + channel);
        n = channel;
    }
    public static String getNotificationChannel() {
        Logger.d("Jarvis", "[FUNC] Storage::getNotificationChannel - " + n);
        return n;
    }
    public static String getNotificationServiceChannel() {
        Logger.d("Jarvis", "[FUNC] Storage::getNotificationServiceChannel - " + n + "_SERVICE");
        return n + "_SERVICE";
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        Storage.activity = activity;
    }
}
