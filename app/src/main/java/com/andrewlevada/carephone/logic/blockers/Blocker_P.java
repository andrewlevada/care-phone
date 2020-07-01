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
    private PhoneCallListener listener;

    private int prevPhoneState;

    public Blocker_P() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationFactory.getInstance(this).pushServiceNotification(this);

        Toolbox.fastLog("REGISTERING LISTENER");
        listener = new PhoneCallListener();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        prevPhoneState = TelephonyManager.CALL_STATE_IDLE;
        logger = new DefaultLogger();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationFactory.getInstance(this).cancelNotification();
        telephony.listen(listener, PhoneStateListener.LISTEN_NONE);
        stopSelf();
    }

    public class PhoneCallListener extends PhoneStateListener {
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
                Toolbox.fastLog(e.getMessage());
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
