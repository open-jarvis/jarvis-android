package com.example.jarvis.calls;

public abstract class CallManager {
    public abstract void onIngoingCall(CallReceiver callReceiver, String callerNumber, String callerName);
    public abstract void  onMissedCall(CallReceiver callReceiver, String callerNumber, String callerName);
    public abstract void      onReject(CallReceiver callReceiver);
    public abstract void      onAccept(CallReceiver callReceiver);
    public abstract void       onEnded(CallReceiver callReceiver);
}
