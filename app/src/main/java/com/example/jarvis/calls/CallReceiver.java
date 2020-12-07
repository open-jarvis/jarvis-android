package com.example.jarvis.calls;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.TelecomManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.jarvis.Jarvis;
import com.example.jarvis.utils.Logger;
import com.example.jarvis.utils.Storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CallReceiver extends BroadcastReceiver {
    private static String lastState = "IDLE";
    public static CallManager callManager;
    public static TelecomManager telecomManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String newState = intent.getStringExtra("state");
        String callerNumber = intent.getStringExtra("incoming_number");

        if (callerNumber == null) {
            return; // in the first call the callerNumber is somehow empty...
        }

        String callerName = getContactName2(callerNumber, Storage.getContext());

        Logger.i("Jarvis", "[CALL] old state: " + lastState + " ; new state: " + newState);

        if (CallReceiver.callManager == null) {
            Logger.i("Jarvis", "[CALL] no JarvisCallManager defined yet.");
            return;
        }

        if (lastState.equals("IDLE")) {
            if (newState.equals("RINGING")) {
                ////////// INCOMING CALL
                callManager.onIngoingCall(this, callerNumber, callerName);
            } else if (newState.equals("OFFHOOK")) {
                ////////// CURRENTLY IN A CALL (is this possible?)
                callManager.onAccept(this);
            } else if (newState.equals("IDLE")) {
                ////////// MISSED CALL
                callManager.onMissedCall(this, callerNumber, callerName);
            }
        } else if (lastState.equals("RINGING")) {
            if (newState.equals("IDLE")) {
                ////////// REJECTED CALL
                callManager.onReject(this);
            } else if (newState.equals("OFFHOOK")) {
                ////////// ACCEPTED CALL
                callManager.onAccept(this);
            }
        } else if (lastState.equals("OFFHOOK")) {
            if (newState.equals("RINGING")) {
                ////////// RINGING AGAIN (is this possible?)
                callManager.onIngoingCall(this, callerNumber, callerName);
            } else if (newState.equals("IDLE")) {
                ////////// STOPPED CALL
                callManager.onEnded(this);
            }
        }
    }

    public void acceptCall() {
        if (CallReceiver.telecomManager == null) {
            Logger.w("Jarvis", "[CALL] acceptCall: no telecomManager");
            return;
        }

        try {
            if (ActivityCompat.checkSelfPermission(Storage.getContext(), Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            CallReceiver.telecomManager.acceptRingingCall();
            //
        } catch (Exception e) {
            Logger.e("Jarvis", "[CALL] acceptCall: " + Log.getStackTraceString(e));
        }
    }

    public void endCall() {
        if (CallReceiver.telecomManager == null) {
            Logger.w("Jarvis", "[CALL] endCall: no telecomManager");
            return;
        }

        if (ActivityCompat.checkSelfPermission(Storage.getContext(), Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        CallReceiver.telecomManager.endCall();
    }

    public void rejectCall() {
        if (CallReceiver.telecomManager == null) {
            Logger.w("Jarvis", "[CALL] rejectCall: no telecomManager");
            return;
        }
        // CallReceiver.telecomManager.isInCall();
        // CallReceiver.telecomManager.silenceRinger();
        this.endCall();
    }


    ////////////////////////////////////////////////////////////////    READ CONTACTS
    private static String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor = context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
    private static String getContactName2(String phoneNumber, Context context) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

}