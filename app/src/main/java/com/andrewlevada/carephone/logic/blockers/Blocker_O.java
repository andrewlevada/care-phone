package com.andrewlevada.carephone.logic.blockers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.GsonBuilder;

import java.util.Arrays;

@RequiresApi(26)
public class Blocker_O extends NotificationListenerService {
    FirebaseRemoteConfig remoteConfig;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toolbox.fastLog("Notification service OnStartCommand");
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification barNotification) {
        try {
            Toolbox.fastLog("Got notification: " + barNotification.getPackageName());

            if (isCallPackage(barNotification.getPackageName())) {
                Toolbox.fastLog("Correct");
                Notification notification = barNotification.getNotification();

                // Analytics
                FirebaseAnalytics.getInstance(this).setUserProperty(
                        Config.Analytics.userPropertyNotificationActions,
                        Toolbox.arrayToString(notification.actions));

                // Process actions
                for (Notification.Action action : notification.actions) {
                    String title = String.valueOf(action.title);
                    Toolbox.fastLog("Got title: " + title);

                    if (isCallDeclineAction(title)) {
                        endCall(action.actionIntent);
                        notification.when = System.currentTimeMillis() + 10000;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Toolbox.fastLog("Listener connected");

        remoteConfig = FirebaseRemoteConfig.getInstance();

        Service itself = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                Toolbox.fastLog("I AM IN");
                NotificationFactory.getInstance(itself).pushServiceNotification(itself);

                itself.stopSelf();
                requestRebind(new ComponentName(itself, Blocker_O.class));

                try {
                    sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Toolbox.fastLog("I AM OUT");
            }
        };
        thread.start();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Toolbox.fastLog("Listener disconnected");
    }

    public void endCall(PendingIntent declineIntent) {
        try {
            Toolbox.fastLog("Ending call");
            declineIntent.send();
            remoteConfig.fetchAndActivate();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toolbox.fastLog("DESTROYING");
        NotificationFactory.getInstance(this).cancelNotification();
    }

    private boolean isCallPackage(String packageName) {
        String serializedArray = remoteConfig.getString(
                Config.Analytics.remoteConfigCallNotificationPackages);

        return Arrays.stream(new GsonBuilder().create().fromJson(serializedArray, String[].class))
                .anyMatch(callPackageHash -> callPackageHash.equals(packageName.toLowerCase()));
    }

    private boolean isCallDeclineAction(String action) {
        String serializedArray = remoteConfig.getString(
                Config.Analytics.remoteConfigCallNotificationDeclineActions);

        return Arrays.stream(new GsonBuilder().create().fromJson(serializedArray, String[].class))
                .anyMatch(declineAction -> declineAction.equals(action.toLowerCase()));
    }
}