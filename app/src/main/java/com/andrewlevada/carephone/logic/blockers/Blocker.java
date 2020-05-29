package com.andrewlevada.carephone.logic.blockers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.andrewlevada.carephone.logic.WhitelistAccesser;

public abstract class Blocker {
    abstract void declineCall(Context context);
    abstract void continueCall(Context context);

    private static Blocker blocker;

    public static boolean enable() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) blocker = new Blocker_N_MR1();

        return blocker != null;
    }

    public static class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1
                    || intent.getAction() == null
                    || !intent.getAction().equals("android.intent.action.PHONE_STATE")) return;

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras() != null ?
                    intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;

            if (state == null || !state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) return;

            if (number == null || !WhitelistAccesser.isInList(number)) blocker.declineCall(context);
            else blocker.continueCall(context);
        }
    }
}
