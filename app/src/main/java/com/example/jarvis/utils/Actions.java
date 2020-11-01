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

import com.example.jarvis.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Actions {
    public static View.OnClickListener onConnectButtonPressed(EditText ipOrHostname, EditText tokenInput, TextView connectionState) {
        return v -> {
            Thread t = new Thread(() -> {
                Utils.hideKeyboard();

                String host = String.valueOf(ipOrHostname.getText());
                String token = String.valueOf(tokenInput.getText());

                String result = null;
                try {
                    result = HTTP.get("http://" + host + ":1884/register-device?token=" + token + "&type=mobile&is_app=true&name=Android Mobile");
                } catch (IOException e) {
                    Log.e("Jarvis", "[HTTP GET] " + "http://" + host + ":1884/register-device?token=" + token + " - " + Log.getStackTraceString(e));
                    e.printStackTrace();
                    return;
                }

                try {
                    JSONObject res = new JSONObject(result);
                    if (res.getBoolean("success")) {
                        // We're registered

                        connectionState.setText("Connected to Jarvis at " + host);
                        connectionState.setTextColor(0xFF1DC558); // R.color.green
                    }
                } catch (JSONException e) {
                    Log.e("Jarvis", "[HTTP JSON] " + "http://" + host + ":1884/register-device?token=" + token + " - " + Log.getStackTraceString(e));
                    e.printStackTrace();
                    return;
                }
            });
            t.start();
        };
    }
}
