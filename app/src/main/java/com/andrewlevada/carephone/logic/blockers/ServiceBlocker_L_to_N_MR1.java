package com.andrewlevada.carephone.logic.blockers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.activities.HelloActivity;
import com.andrewlevada.carephone.activities.LogFragment;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.network.Network;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.Date;

public class ServiceBlocker_L_to_N_MR1 extends Service {
    private NotificationManager notificationManager;
    public static final int DEFAULT_NOTIFICATION_ID = 159;

    private IncomingCallReceiver receiver;

    private String prevPhoneState;
    private Date callStartTime;
    private int callType;

    public ServiceBlocker_L_to_N_MR1() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification("HEEELLO", getString(R.string.app_name), getString(R.string.service_notification_text));

        Toolbox.FastLog("REGISTERING RECEIVER");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        receiver = new IncomingCallReceiver();
        registerReceiver(receiver, intentFilter);

        prevPhoneState = TelephonyManager.EXTRA_STATE_IDLE;

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Toolbox.FastLog("DESTROYING");
        unregisterReceiver(receiver);
        notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
        super.onDestroy();
    }

    public void sendNotification(String ticker, String title, String text) {
        Intent notificationIntent = new Intent(this, HelloActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: Deal wth notification channels and add big icon
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setOngoing(true)
                .setSmallIcon(R.drawable.outline_icon)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis());

        Notification notification = builder.build();
        startForeground(DEFAULT_NOTIFICATION_ID, notification);
    }

    public class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.PHONE_STATE"))
                return;

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String phone = intent.getExtras() != null ?
                    intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;

            logAction(phone, state);
            Toolbox.FastLog("STATE: " + state);

            if (state == null || !state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
                return;

            if (phone == null) {
                declineCall(context);
                return;
            }

            WhitelistAccesser.getInstance().doDeclineCall(phone, arg -> {
                if (arg) declineCall(context);
            });
        }
    }

    private static void declineCall(Context context) {
        try {
            Toolbox.FastLog("BLOCKING");
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) return;

            @SuppressLint("SoonBlockedPrivateApi")
            Method m = telephonyManager.getClass().getDeclaredMethod("getITelephony");

            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);
            if (telephonyService == null) return;
            telephonyService.endCall();
            Toolbox.FastLog("BLOCKED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logAction(String phone, String state) {
        if (prevPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING) &&
            state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            callStartTime = new Date(System.currentTimeMillis());
            callType = LogFragment.TYPE_INCOMING;
        } else if (prevPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE) &&
                state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            callStartTime = new Date(System.currentTimeMillis());
            callType = LogFragment.TYPE_OUTGOING;
        } else if (prevPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK) &&
            state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                int duration = (int)((System.currentTimeMillis() - callStartTime.getTime()) / 1000);
                Network.cared().addToLog(new LogRecord(phone, callStartTime.getTime(), duration, callType), null);
                callStartTime = null;
                callType = -1;
            }

        prevPhoneState = state;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toolbox.FastLog("UNBIND");
        return super.onUnbind(intent);
    }
}
