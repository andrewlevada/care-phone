package com.andrewlevada.carephone.logic.blockers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class Blocker_N_MR1 extends Blocker {

    @Override
    public void initiateBlocking() {
        // Receiver is processed in manifest
    }

    @Override
    public void onDestroy() {
        // Receiver is processed in manifest
    }

    public static class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1
                || intent.getAction() == null
                || !intent.getAction().equals("android.intent.action.PHONE_STATE")) return;

            try {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras() != null ?
                        intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;

                if (state == null || !state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) return;

                TelephonyManager telephonyManager =
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager == null) return;

                @SuppressLint("SoonBlockedPrivateApi")
                Method m = telephonyManager.getClass().getDeclaredMethod("getITelephony");

                m.setAccessible(true);
                ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);
                if (telephonyService == null) return;

                if (number == null || !WhitelistAccesser.isInList(number)) {
                    telephonyService.endCall();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Blocker_N_MR1(Context context) {
        super(context);
    }
}
