/*
 * The MIT License
 *
 * Copyright 2018 Sonu Auti http://sonuauti.com twitter @SonuAuti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.example.jarvis.ext.web;

import android.util.Log;

import com.example.jarvis.utils.Storage;
import com.example.jarvis.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

/**
 *
 * @author cis
 */
public class AppApis {

    public AppApis() {
    }

    public String ping(HashMap qparms) {
        Log.d("Jarvis", "[HTTP SERVER] GET /ping");
        return "pong";
    }

    public String deviceType(HashMap qparms) {
        return "phone";
    }

    public String getNotifications(HashMap qparms) {
        Log.d("Jarvis", "[HTTP SERVER] GET /getNotifications");
        try {
            if (Storage.getNotifications() == null) {
                return "no-notifications";
            } else {
                TinyWebServer.CONTENT_TYPE = "application/json";
                JSONArray jsonArray = new JSONArray(Storage.getNotifications().toArray());
                String result = jsonArray.toString();
                Log.d("Jarvis", "[HTTP SERVER] GET /getNotifications - " + result);
                return result;
            }
        } catch (JSONException e) {
            Log.d("Jarvis", "[HTTP SERVER] GET /getNotifications - Error: " + Log.getStackTraceString(e));
            return "unknown-error";
        }
    }
}
