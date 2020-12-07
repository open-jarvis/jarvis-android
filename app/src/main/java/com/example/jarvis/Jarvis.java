package com.example.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.jarvis.utils.HTTP;
import com.example.jarvis.utils.HTTPAnswer;
import com.example.jarvis.utils.Logger;
import com.example.jarvis.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Jarvis {
    private static final int PORT = 2021;
    private static String host, preSharedKey, token;
    private static boolean connected = false;

    public static void connectTo(String host, String preSharedKey, String token) {
        Jarvis.host = host;
        Jarvis.preSharedKey = preSharedKey;
        Jarvis.token = token;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Jarvis.connected) {
                        MainActivity.setConnectionText("Already connected to Jarvis at " + Jarvis.host);
                        return;
                    }

                    String response = HTTP.post(Jarvis.getUrl() + "/register-device?name=Android Mobile&token=" + token + "&type=mobile&native=true", "{\"psk\":\"" + Jarvis.preSharedKey + "\"}");
                    JSONObject jsonResponse = new JSONObject(response);
                    Jarvis.connected = jsonResponse.getBoolean("success");

                    if (jsonResponse.getBoolean("success")) {
                        MainActivity.setConnectionText("Connected to Jarvis at " + Jarvis.host);
                        MainActivity.setConnectionColor(0xFF1DC558);

                        Jarvis.storeCredentials();
                        Jarvis.startHelloThread();
                    } else {
                        Jarvis.reconnect();
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("Jarvis", "[JARVIS] connectTo(" + host + ", <preSharedKey>) - " + Log.getStackTraceString(e));
                }
            }
        }).start();
    }
    public static void reconnect() {
        String data = Jarvis.readFromFile(Storage.getContext());
        if (data != "") {
            try {
                JSONObject credentials = new JSONObject(data);

                Jarvis.host = credentials.getString("host");
                Jarvis.token = credentials.getString("token");
                Jarvis.preSharedKey = credentials.getString("psk");

                try {
                    if (MainActivity.jarvisToken.getText().toString() == "") {
                        MainActivity.jarvisToken.setText(Jarvis.token);
                        MainActivity.preSharedKeyInput.setText(Jarvis.preSharedKey);
                        MainActivity.ipOrHostname.setText(Jarvis.host);
                    }
                } catch (Exception e) {
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String response2 = HTTP.post(Jarvis.getUrl() + "/am-i-registered?token=" + Jarvis.token, "{\"psk\":\"" + Jarvis.preSharedKey + "\"}");
                            JSONObject jsonResponse2 = new JSONObject(response2);
                            Jarvis.connected = jsonResponse2.getBoolean("success");

                            if (jsonResponse2.getBoolean("success")) {
                                MainActivity.setConnectionText("Reconnected to Jarvis at " + Jarvis.host);
                                MainActivity.setConnectionColor(0xFF1DC558);
                                Jarvis.storeCredentials();
                                Jarvis.startHelloThread();
                            } else {
                                MainActivity.setConnectionText("Failed to connect to Jarvis at " + Jarvis.host);
                                MainActivity.setConnectionColor(0xFFFF3F3F);
                                Logger.e("Jarvis", "[JARVIS] Wasn't connected to Jarvis server before.");
                            }
                        } catch (Exception e) {
                            Logger.e("Jarvis", "[RECONNECT] Exception: " + Log.getStackTraceString(e));
                            MainActivity.setConnectionText("Something went wrong while connecting...");
                            MainActivity.setConnectionColor(0xFFFF3F3F);
                        }
                    }
                }).start();
            } catch (Exception e) {
                Logger.e("Jarvis", "[JSON] Failed to read credentials: " + Log.getStackTraceString(e));
            }
        }
    }

    public static void ask(String type, String name, String infos, Set<Map<String, String>> options, HTTPAnswer onAnswer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String body = "{\"type\":\"" + type + "\", \"name\":\"" + name + "\", \"infos\":\"" + infos + "\", \"options\":" + new JSONArray(options).toString() + "}";
                    String response = HTTP.post(Jarvis.getUrl() + "/id/ask?token=" + token, body);
                    JSONObject jsonResponse = new JSONObject(response);
                    if (!jsonResponse.getBoolean("success")) {
                        Logger.w("Jarvis", "[ASK] Failed to ask: " + jsonResponse.getString("error"));
                    } else {
                        Logger.i("Jarvis", "[ASK] asked");

                        Timer answerTimer = new Timer();
                        answerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (!Jarvis.connected) { return; }
                                try {
                                    String body = "{\"psk\":\""+Jarvis.preSharedKey+"\",\"target_token\":\""+ Jarvis.token +"\",\"type\":\"" + type + "\"}";
                                    String response = HTTP.post(Jarvis.getUrl() + "/id/scan?token=" + Jarvis.token, body);
                                    // Logger.i("Jarvis", "[ANSWER_THREAD] + " + response);
                                    onAnswer.run(response);
                                    answerTimer.cancel();
                                    answerTimer.purge();
                                } catch (Exception e) {
                                    Logger.e("Jarvis", "[ANSWER_THREAD] " + Log.getStackTraceString(e));
                                }
                            }
                        }, 0, 200);
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("Jarvis", "[HTTP] " + Log.getStackTraceString(e));
                }
            }
        }).start();

    }
    public static void delete(String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String body = "{\"psk\":\"" + Jarvis.preSharedKey + "\",\"target_token\":\"" + Jarvis.token + "\", \"type\":\"" + type + "}";
                    String response = HTTP.post(Jarvis.getUrl() + "/id/delete?token=" + token, body);
                    Logger.i("Jarvis", "[ASK_DELETE] successfully deleted: " + response);
                } catch (Exception e) {
                    Logger.e("Jarvis", "[ASK_DELETE] error:" + Log.getStackTraceString(e));
                }
            }
        }).start();
    }


    public static void startHelloThread() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Jarvis.connected) {
                    try {
                        HTTP.post(Jarvis.getUrl() + "/hello?token=" + Jarvis.token, "{\"psk\":\"" + Jarvis.preSharedKey + "\"}");
                    } catch (IOException e) {
                        Logger.e("", "");
                    }
                }
            }
        }, 0, 15_000);
    }
    public static void startLocationThread() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Jarvis.isConnected()) {
                    LocationManager locationManager = (LocationManager) Storage.getContext().getSystemService(Context.LOCATION_SERVICE);
                    try {
                        if (ActivityCompat.checkSelfPermission(Storage.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                            // ActivityCompat.requestPermissions(Storage.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, 0);
                            return;
                        }

                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);

                        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));

                        Map<String, String> locationData = new HashMap<>();
                        locationData.put("lat", String.valueOf(location.getLatitude()));
                        locationData.put("lon", String.valueOf(location.getLongitude()));
                        locationData.put("accuracy", String.valueOf(location.getAccuracy()));
                        locationData.put("provider", location.getProvider());
                        locationData.put("altitude", String.valueOf(location.getAltitude()));
                        locationData.put("speed", String.valueOf(location.getSpeed()));
                        locationData.put("timestamp", String.valueOf(location.getTime()));

                        HTTP.post(Jarvis.getUrl() + "/set-location?token=" + Jarvis.token, "{\"psk\":\"" + Jarvis.preSharedKey + "\", \"location\":" + new JSONObject(locationData).toString() + "}");
                    } catch (Exception e) {
                        Logger.e("Jarvis", "[GPS] Exception: " + Log.getStackTraceString(e));
                    }
                }
            }
        }, 0,10_000);
    }

    public static void sendBatteryState(Map<String, String> battery) {
        new Thread(() -> {
            if (!Jarvis.isConnected()) {
                attemptReconnect("sendBatteryState");
            }

            try {
                String result = "{\"psk\":\"" + Jarvis.preSharedKey + "\",\"property\":\"battery\",\"value\":" + new JSONObject(battery).toString() + "}";
                String response = HTTP.post(Jarvis.getUrl() + "/set-property?token="  + Jarvis.token, result);

                JSONObject jsonResponse = new JSONObject(response);
            } catch (Exception e) {
                Logger.e("Jarvis", "[JSON] Error: sendNotifications() - " + Log.getStackTraceString(e));
                return;
            }
        }).start();
    }
    public static void sendNotifications(Set<Map<String, String>> notifications) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Jarvis.isConnected()) {
                    attemptReconnect("sendNotifications");
                }
                String result = null;
                try {
                    result = "{\"psk\":\"" + Jarvis.preSharedKey + "\",\"property\":\"notifications\",\"value\":{\"notifications\":" + new JSONArray(notifications.toArray()).toString() + ",\"last-update\":" + Math.round(System.currentTimeMillis() / 1000L) + "}}";
                    String response = HTTP.post(Jarvis.getUrl() + "/set-property?token="  + Jarvis.token, result);

                    JSONObject jsonResponse = new JSONObject(response);
                } catch (JSONException | IOException e) {
                    Logger.e("Jarvis", "[JSON] Error: sendNotifications() - " + Log.getStackTraceString(e));
                    return;
                }
            }
        }).start();
    }



    ////////////////////////////////////////////////////////////////    GETTER
    public static boolean isConnected() {
        return Jarvis.connected;
    }
    public static String getHost() {
        return Jarvis.host;
    }
    public static String getToken() {
        return Jarvis.token;
    }


    ////////////////////////////////////////////////////////////////    PRIVATE FUNCTIONS
    private static String getUrl() {
        return getUrl(Jarvis.host);
    }
    private static String getUrl(String host) {
        return "http://" + host + ":" + Jarvis.PORT;
    }


    private static void attemptReconnect(String from) {
        Logger.d("Jarvis", "[JARVIS] " + from + ": Jarvis is not connected");
        if (Jarvis.credentialsAvailable()) {
            Logger.d("Jarvis", "[JARVIS] " + from + ": Attempting reconnect");

            Jarvis.reconnect();
            return;
        }
    }
    private static boolean credentialsAvailable() {
        Context context = Storage.getContext();
        if (context == null) {
            return false;
        }
        String data = Jarvis.readFromFile(context);
        return data != "";
    }
    private static boolean storeCredentials() {
        Map<String, String> credentials = new HashMap<>();

        credentials.put("host", Jarvis.host);
        credentials.put("psk", Jarvis.preSharedKey);
        credentials.put("token", Jarvis.token);

        return Jarvis.writeToFile(new JSONObject(credentials).toString(), Storage.getContext());
    }

    private static boolean writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("jarvis.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        }
        catch (IOException e) {
            Logger.e("Jarvis", "[FILE] Write failed: " + Log.getStackTraceString(e));
            return false;
        }
    }
    private static String readFromFile(Context context) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("jarvis.json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (Exception e) {
            Logger.e("Jarvis", "[FILE] Read failed: " + Log.getStackTraceString(e));
        }

        return ret;
    }
    ////////////////////////////////////////////////////////////////
}
