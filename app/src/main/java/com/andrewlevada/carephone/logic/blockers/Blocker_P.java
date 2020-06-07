package com.andrewlevada.carephone.logic.blockers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.activities.HelloActivity;
import com.andrewlevada.carephone.activities.LogFragment;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.network.Network;

import java.util.Date;

@RequiresApi(28)
class Blocker_P extends Service {
    private NotificationManager notificationManager;
    private TelephonyManager telephony;
    public static final int DEFAULT_NOTIFICATION_ID = 159;

    private int prevPhoneState;
    private Date callStartTime;
    private int callType;

    public Blocker_P() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification("HEEELLO", getString(R.string.app_name), getString(R.string.service_notification_text));

        Toolbox.FastLog("REGISTERING LISTENER");
        PhoneCallListener listener = new PhoneCallListener();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony != null) telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        prevPhoneState = TelephonyManager.CALL_STATE_IDLE;

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Toolbox.FastLog("DESTROYING");
        telephony.listen(null, PhoneStateListener.LISTEN_NONE);
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

    private class PhoneCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Toolbox.FastLog("numberhere: " + incomingNumber);

            logAction(incomingNumber, state);

            if (state != TelephonyManager.CALL_STATE_RINGING) return;

            if (incomingNumber == null) {
                declineCall();
                return;
            }

            WhitelistAccesser.getInstance().doDeclineCall(incomingNumber, arg -> {
                if (arg) declineCall();
            });
        }
    }

    private void logAction(String phone, int state) {
        if (phone == null) return;
        if (prevPhoneState == TelephonyManager.CALL_STATE_RINGING &&
                state == TelephonyManager.CALL_STATE_OFFHOOK) {
            callStartTime = new Date(System.currentTimeMillis());
            callType = LogFragment.TYPE_INCOMING;
        } else if (prevPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                state == TelephonyManager.CALL_STATE_OFFHOOK) {
            callStartTime = new Date(System.currentTimeMillis());
            callType = LogFragment.TYPE_OUTGOING;
        } else if (prevPhoneState == TelephonyManager.CALL_STATE_OFFHOOK &&
                state == TelephonyManager.CALL_STATE_IDLE) {
            int duration = (int)((System.currentTimeMillis() - callStartTime.getTime()) / 1000);
            Network.cared().addToLog(new LogRecord(phone, callStartTime.getTime(), duration, callType), null);
            callStartTime = null;
            callType = -1;
        }

        prevPhoneState = state;
    }

    void declineCall() {
        Toolbox.FastLog("IN");
        try {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) Toolbox.FastLog("SR NL");
            else telecomManager.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
