package com.example.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.jarvis.utils.Logger;
import com.example.jarvis.utils.StatusBarNotificationInfo;
import com.example.jarvis.utils.Storage;
import com.example.jarvis.utils.Utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationService extends NotificationListenerService {
    public static NotificationServiceReceiver notificationServiceReceiver;

    @Override
    public void onCreate() {
        Logger.d("Jarvis", "[FUNC] NotificationService::onCreate");
        super.onCreate();
        notificationServiceReceiver = new NotificationServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Storage.getNotificationServiceChannel());
        registerReceiver(notificationServiceReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Logger.d("Jarvis", "[FUNC] NotificationService::onDestroy");
        super.onDestroy();
        unregisterReceiver(notificationServiceReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Jarvis.sendNotifications(notificationServiceReceiver.getNotifications());

        Intent i = new Intent(Storage.getNotificationChannel());
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Jarvis.sendNotifications(notificationServiceReceiver.getNotifications());

        Intent i = new Intent(Storage.getNotificationChannel());
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }

    public class NotificationServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Jarvis.isConnected()) { Logger.i("Jarvis", "onReceive : Notifications stopped"); return; }
            Jarvis.sendNotifications(getNotifications());

            Logger.d("Jarvis", "[FUNC] NotificationService.NotificationServiceReceiver::onReceive - " + intent.getStringExtra("command"));
            if (intent.getStringExtra("command").equals("clearall")) {
                NotificationService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")) {
                Intent i1 = new Intent(Storage.getNotificationChannel());
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);

                int i=1;
                for (StatusBarNotification sbn : NotificationService.this.getActiveNotifications()) {
                    StatusBarNotificationInfo sInfo = new StatusBarNotificationInfo(sbn);

                    Intent i2 = new Intent(Storage.getNotificationChannel());
                    i2.putExtra("notification_event",i +" " + sInfo.getTimeStamp() + " " + sInfo.getSender() + " " + sInfo.getTitle() + " " + sInfo.getMessage() + "\n");
                    sendBroadcast(i2);
                    i++;
                }

                Intent i3 = new Intent(Storage.getNotificationChannel());
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);
            }

        }

        public Set<Map<String, String>> getNotifications() {
            Logger.d("Jarvis", "[FUNC] NotificationService::getNotifications - Getting and saving Notifications");

            HashSet<Map<String, String>> result = new HashSet<>();

            for (StatusBarNotification sbn : NotificationService.this.getActiveNotifications()) {
                StatusBarNotificationInfo sInfo = new StatusBarNotificationInfo(sbn);

                result.add(sInfo.getAllInfos());
            }

            return result;
        }
    }
}
