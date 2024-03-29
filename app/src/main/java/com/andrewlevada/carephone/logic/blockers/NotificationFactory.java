package com.andrewlevada.carephone.logic.blockers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.andrewlevada.carephone.R;

class NotificationFactory {
    public static final int DEFAULT_NOTIFICATION_ID = 159;
    private static final String NOTIFICATION_CHANNEL_ID = "CarePhone_Service_NC";
    private static final String NOTIFICATION_CHANNEL_NAME = "CarePhoneServiceNotificationChannel";

    private static NotificationManager notificationManager;
    private static NotificationFactory instance;

    public NotificationFactory(Context context) {
        notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
    }

    public Notification getNotification(Service context) {
//        Intent notificationIntent = new Intent(context, HelloActivity.class);
//        notificationIntent.setAction(Intent.ACTION_MAIN);
//        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setOngoing(true)
//                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.outline_icon)
                .setTicker("")
                .setContentTitle(context.getString(R.string.service_notification_text))
                .setContentText(context.getString(R.string.service_notification_text))
                .setWhen(System.currentTimeMillis());

        return builder.build();
    }

    public void pushServiceNotification(Service context) {
        context.startForeground(DEFAULT_NOTIFICATION_ID, getNotification(context));
    }

    public void cancelNotification() {
        notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChanel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        channel.setLightColor(context.getResources().getColor(R.color.colorPrimary));
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setShowBadge(true);

        notificationManager.createNotificationChannel(channel);
    }

    private NotificationCompat.Builder getNotificationBuilder(Context context) {
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel(context);
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        } else builder = new NotificationCompat.Builder(context);

        return builder;
    }

    public static NotificationFactory getInstance(Context context) {
        if (instance == null) instance = new NotificationFactory(context);
        return instance;
    }
}
