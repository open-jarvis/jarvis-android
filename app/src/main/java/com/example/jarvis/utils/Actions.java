package com.example.jarvis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarvis.Jarvis;
import com.example.jarvis.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Actions {
    public static View.OnClickListener onConnectButtonPressed(EditText ipOrHostname, EditText tokenInput, TextView connectionState, EditText preSharedKeyInput) {
        return v -> {
            Utils.hideKeyboard();

            String host = String.valueOf(ipOrHostname.getText());
            String preSharedKey = String.valueOf(preSharedKeyInput.getText());
            String token = String.valueOf(tokenInput.getText());

            Jarvis.connectTo(host, preSharedKey, token);
        };
    }

    public static View.OnClickListener onReconnectButtonPressed() {
        return v -> {
            Jarvis.reconnect();
        };
    }
}
