package com.andrewlevada.carephone.logic.blockers;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.android.internal.telephony.ITelephony;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.lang.reflect.Method;

public class Blocker_L_to_N_MR1 extends Service {
    private static Blocker_L_to_N_MR1 instance;

    private DefaultLogger logger;
    private IncomingCallReceiver receiver;

    private String prevPhoneState;

    public Blocker_L_to_N_MR1() { }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;

        NotificationFactory.getInstance(this).pushServiceNotification(this);

        Toolbox.fastLog("REGISTERING RECEIVER");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        receiver = new IncomingCallReceiver();
        registerReceiver(receiver, intentFilter);

        prevPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
        logger = new DefaultLogger();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toolbox.fastLog("DESTROYING");
        NotificationFactory.getInstance(this).cancelNotification();
        unregisterReceiver(receiver);
    }

    public class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.PHONE_STATE"))
                    return;

                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String phone = intent.getExtras() != null ?
                        intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;

                Toolbox.fastLog("CHANGE: " + phone);
                Toolbox.fastLog("STATE: " + state);
                if (state == null) return;

                logAction(phone, state);

                if (!state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) return;

                if (phone == null) {
                    declineCall(context);
                    logger.onBlocked(null);
                    return;
                }

                if (WhitelistAccesser.getInstance().doDeclineCall(phone)) {
                    declineCall(context);
                    logger.onBlocked(phone);
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                // TODO: Show Unsupported message
            }
        }
    }

    private static void declineCall(Context context) {
        try {
            Toolbox.fastLog("BLOCKING CALL");
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) return;

            @SuppressLint("SoonBlockedPrivateApi")
            Method m = telephonyManager.getClass().getDeclaredMethod("getITelephony");

            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);
            if (telephonyService == null) return;
            telephonyService.endCall();
            Toolbox.fastLog("BLOCKED");
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            // TODO: Show Unsupported message
        }
    }

    private void logAction(String phone, String state) {
        if (prevPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING) &&
            state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK))
            logger.onIncomingStart();
        else if (prevPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE) &&
                state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK))
            logger.onOutgoingStart();
        else if (prevPhoneState.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK) &&
            state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE))
            logger.onCurrentEnd(phone);

        prevPhoneState = state;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void tryStop() {
        if (instance != null) instance.stopSelf();
    }
}
