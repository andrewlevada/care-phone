package com.andrewlevada.carephone.logic.blockers;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

@RequiresApi(28)
public class Blocker_P extends Service {
    private DefaultLogger logger;
    private TelephonyManager telephony;

    private int prevPhoneState;

    public Blocker_P() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Service itself = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                NotificationFactory.getInstance(itself).pushServiceNotification(itself);

                Toolbox.fastLog("REGISTERING LISTENER");
                PhoneCallListener listener = new PhoneCallListener();
                telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (telephony != null) telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

                prevPhoneState = TelephonyManager.CALL_STATE_IDLE;
                logger = new DefaultLogger();
            }
        };
        thread.start();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Toolbox.fastLog("DESTROYING 1");
        super.onDestroy();
        Toolbox.fastLog("DESTROYING 2");
        NotificationFactory.getInstance(this).cancelNotification();
        telephony.listen(null, PhoneStateListener.LISTEN_NONE);
        stopSelf();
    }

    private class PhoneCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                Toolbox.fastLog("CHANGE: " + incomingNumber);
                Toolbox.fastLog("STATE: " + state);

                logAction(incomingNumber, state);

                if (state != TelephonyManager.CALL_STATE_RINGING) return;

                if (incomingNumber == null) {
                    declineCall();
                    return;
                }

                WhitelistAccesser.getInstance().doDeclineCall(incomingNumber, arg -> {
                    if (arg) declineCall();
                });
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                // TODO: Show Unsupported message
            }
        }
    }

    private void logAction(String phone, int state) {
        if (phone == null) return;

        if (prevPhoneState == TelephonyManager.CALL_STATE_RINGING &&
                state == TelephonyManager.CALL_STATE_OFFHOOK)
            logger.onIncomingStart();
        else if (prevPhoneState == TelephonyManager.CALL_STATE_IDLE &&
                state == TelephonyManager.CALL_STATE_OFFHOOK)
            logger.onOutgoingStart();
        else if (prevPhoneState == TelephonyManager.CALL_STATE_OFFHOOK &&
                state == TelephonyManager.CALL_STATE_IDLE)
            logger.onCurrentEnd(phone);

        prevPhoneState = state;
    }

    void declineCall() {
        try {
            Toolbox.fastLog("BLOCKING CALL");

            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) throw new Exception("TelecomManager is null");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED) return;

            telecomManager.endCall();

            Toolbox.fastLog("BLOCKED");
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            // TODO: Show Unsupported message
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
