package com.andrewlevada.carephone.logic.blockers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;

public abstract class Blocker {
    /**
     * @param context
     * @return If true it processed call itself and onReceiver code should be stopped
     */
    abstract boolean receivedCall(Context context);

    abstract void declineCall(Context context);

    abstract void continueCall(Context context);

    private static Blocker blocker;

    public static boolean enable(Context context) {
        int sdk = Build.VERSION.SDK_INT;

        if (sdk == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN_MR2) return false;
        else if (sdk == Build.VERSION_CODES.KITKAT) return false;
        else if (sdk == Build.VERSION_CODES.LOLLIPOP) blocker = new Blocker_N_MR1();
        else if (sdk == Build.VERSION_CODES.LOLLIPOP_MR1) blocker = new Blocker_N_MR1();
        else if (sdk == Build.VERSION_CODES.M) blocker = new Blocker_N_MR1();
        else if (sdk == Build.VERSION_CODES.N) blocker = new Blocker_N_MR1();
        else if (sdk == Build.VERSION_CODES.N_MR1) blocker = new Blocker_N_MR1();
        else if (sdk == Build.VERSION_CODES.O) return false;
        else if (sdk == Build.VERSION_CODES.O_MR1) return false;
        else if (sdk == Build.VERSION_CODES.P) blocker = new Blocker_P();
        else if (sdk == Build.VERSION_CODES.Q) blocker = new Blocker_P();
        else return false;

        Toolbox.FastLog("INITIATING");
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener,
                PhoneStateListener.LISTEN_CALL_STATE);

        return true;
    }

    public static class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.PHONE_STATE"))
                return;

            if (blocker.receivedCall(context)) return;

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras() != null ?
                    intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;
            Toolbox.FastLog("ph: " + number);

            if (state == null || !state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
                return;

            if (number == null || !WhitelistAccesser.isInList(number)) blocker.declineCall(context);
            else blocker.continueCall(context);
        }
    }

    private static class PhoneCallListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
        Toolbox.FastLog("numberhere: " + incomingNumber);
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Toolbox.FastLog("RINGING, number: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Toolbox.FastLog("OFFHOOK: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {

            }
        }
    }
}