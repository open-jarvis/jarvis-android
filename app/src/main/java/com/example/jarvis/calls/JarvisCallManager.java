package com.example.jarvis.calls;

import android.util.Log;

import com.example.jarvis.Jarvis;
import com.example.jarvis.utils.HTTPAnswer;
import com.example.jarvis.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JarvisCallManager extends CallManager {
    @Override
    public void onIngoingCall(CallReceiver callReceiver, String callerNumber, String callerName) {
        Logger.d("Jarvis", "[CALL] onIngoingCall from " + callerName + "(" + callerNumber + ")");
        Jarvis.ask("call", "Ingoing call", callerName + " (" + callerNumber + ") is calling...", getIncomingCallOptions(), new HTTPAnswer() {
            @Override
            public void run(String answer) {
                try {
                    JSONObject jsonObject = new JSONObject(answer);
                    if (jsonObject.getBoolean("success")) {
                        if (jsonObject.getJSONObject("scan").has(Jarvis.getToken()) && jsonObject.getJSONObject("scan").getJSONArray(Jarvis.getToken()).length() > 0) {
                            JSONArray scanResult = jsonObject.getJSONObject("scan").getJSONArray(Jarvis.getToken());
                            for (int i = 0; i < scanResult.length(); i++) {
                                JSONObject result = scanResult.getJSONObject(i);
                                if (result.getBoolean("answered")) {
                                    JSONObject PICKED_OPTION = result.getJSONObject("answer").getJSONObject("option");
                                    if (PICKED_OPTION.getString("id").equals("ACCEPT")) {
                                        callReceiver.acceptCall();
                                    } else if (PICKED_OPTION.getString("id").equals("REJECT")) {
                                        callReceiver.rejectCall();
                                    }
                                    Jarvis.delete("call");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.e("Jarvis", "[CALL] onIngoingCall::HTTPAnswer.run - " + Log.getStackTraceString(e));
                }
            }
        });
    }

    @Override
    public void onMissedCall(CallReceiver callReceiver, String callerNumber, String callerName) {
        Logger.d("Jarvis", "[CALL] onMissedCall from " + callerName + " (" + callerNumber + ")");
    }

    @Override
    public void onReject(CallReceiver callReceiver) {
        Logger.d("Jarvis", "[CALL] onReject");
    }

    @Override
    public void onAccept(CallReceiver callReceiver) {
        Logger.d("Jarvis", "[CALL] onAccept");
    }

    @Override
    public void onEnded(CallReceiver callReceiver) {
        Logger.d("Jarvis", "[CALL] onEnded");
    }


    private static Set<Map<String, String>> getIncomingCallOptions() {
        Set<Map<String, String>> options = new HashSet<>();

        Map<String, String> accept = new HashMap<>();
        Map<String, String> reject = new HashMap<>();

        accept.put("id", "ACCEPT");
        accept.put("text", "Accept");
        accept.put("color", "#0aa61f");
        accept.put("material", "call");

        reject.put("id", "REJECT");
        reject.put("text", "Reject");
        reject.put("color", "#ff3f3f");
        reject.put("material", "call_end");

        options.add(accept);
        options.add(reject);

        return options;
    }
}
