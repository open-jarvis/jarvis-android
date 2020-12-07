package com.example.jarvis.calls;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/* ONLY FOR API 29+
* TODO: accept calls using the InCallService API
*  */
public class JarvisInCallService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
