package com.andrewlevada.carephone.logic.blockers;

import android.app.Notification;
import android.app.PendingIntent;
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

public class Blocker_O extends NotificationListenerService {
    private static Blocker_O instance;

    FirebaseRemoteConfig remoteConfig;

    @RequiresApi(26)
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
                        if (Blocker_P.doDeclineCurrentCall) {
                            endCall(action.actionIntent);
                            cancelNotification(barNotification.getKey());
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toolbox.registerErrorOccurred(this);
        }
    }

    @Override
    public void onListenerConnected() {
        Toolbox.fastLog("Listener connected");
        instance = this;
        super.onListenerConnected();

        NotificationFactory.getInstance(this).pushServiceNotification(this);
        remoteConfig = FirebaseRemoteConfig.getInstance();
    }

    @Override
    public void onDestroy() {
        Toolbox.fastLog("Blocker_O onDestroy()");
        NotificationFactory.getInstance(this).cancelNotification();
        super.onDestroy();
    }

    public void endCall(PendingIntent declineIntent) {
        try {
            Toolbox.fastLog("Ending call");
            declineIntent.send();
            remoteConfig.fetchAndActivate();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toolbox.registerErrorOccurred(this);
        }
    }

    @RequiresApi(26)
    private boolean isCallPackage(String packageName) {
        String serializedArray = remoteConfig.getString(
                Config.Analytics.remoteConfigCallNotificationPackages);

        return Arrays.stream(new GsonBuilder().create().fromJson(serializedArray, String[].class))
                .anyMatch(callPackageHash -> callPackageHash.equals(packageName.toLowerCase()));
    }

    @RequiresApi(26)
    private boolean isCallDeclineAction(String action) {
        String serializedArray = remoteConfig.getString(
                Config.Analytics.remoteConfigCallNotificationDeclineActions);

        return Arrays.stream(new GsonBuilder().create().fromJson(serializedArray, String[].class))
                .anyMatch(declineAction -> declineAction.equals(action.toLowerCase()));
    }

    public static void tryStop() {
        if (instance != null) instance.stopSelf();
    }
}