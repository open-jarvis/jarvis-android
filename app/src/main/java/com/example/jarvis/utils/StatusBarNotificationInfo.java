package com.example.jarvis.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.example.jarvis.utils.Storage.getContext;

public class StatusBarNotificationInfo {
    private StatusBarNotification s;
    private Context c = null;

    public StatusBarNotificationInfo(StatusBarNotification sbn, Context context) {
        this.s = sbn;
        this.c = context;
    }
    public StatusBarNotificationInfo(StatusBarNotification sbn) {
        this(sbn, null);
    }

    public String getSender() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(this.s.getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException e) {}
            return (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
        } catch (NullPointerException e) {
            Log.e("Jarvis", "[ERR] Need to open app to create a Context");
            return "";
        }
    }

    public String getTitle() {
        return s.getNotification().extras.getString("android.title");
    }

    public String getMessage() {
        return s.getNotification().extras.getString("android.text");
    }

    public long getTimeStamp() {
        return s.getPostTime();
    }

    public String getIcon() {
        Drawable drawable = iconToDrawable(getIconForPackageName());
        Bitmap icon = drawableToBitmap(drawable);

        if (icon == null) {
            return "";
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(byteArray);
    }

    public Map<String, String> getAllInfos() {
        Map<String, String> result = new HashMap<>();

        result.put("timestamp", String.valueOf(getTimeStamp()));
        result.put("sender", getSender());
        result.put("title", getTitle());
        result.put("message", getMessage());
        result.put("icon", getIcon());

        return result;
    }

    private Icon getIconForPackageName() {
        return s.getNotification().getSmallIcon();
    }

    private Drawable getIconForPackageName2() {
        try {
            return Storage.getContext().getPackageManager().getApplicationIcon(s.getPackageName());
        } catch (NullPointerException e) {
            Log.e("Jarvis", "[ERR] Need to open app to create a Context");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Jarvis", "getIconForPackageName2 - " + Log.getStackTraceString(e));
        }
        return null;
    }

    private Drawable iconToDrawable(Icon icon) {
        if (icon == null) {
            return getIconForPackageName2();
        }

        Drawable drawable = null;
        try {
            drawable = icon.loadDrawable(getContext());
        } catch (NullPointerException e) {
            Log.e("Jarvis", "[ERR] Need to open app to create a Context");
        }

        return drawable;
    }

    private Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
