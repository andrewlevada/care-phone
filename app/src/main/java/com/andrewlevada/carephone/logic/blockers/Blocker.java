package com.andrewlevada.carephone.logic.blockers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.andrewlevada.carephone.Toolbox;

public class Blocker {

    public static boolean enable(Context context) {
        Intent blockerIntent;
        Class<?> blockerClass;
        int sdk = Build.VERSION.SDK_INT;

        if (sdk == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN_MR2) return false;
        else if (sdk == Build.VERSION_CODES.KITKAT) return false;
        else if (sdk >= Build.VERSION_CODES.LOLLIPOP && sdk <= Build.VERSION_CODES.N_MR1) {
            blockerIntent = new Intent(context, ServiceBlocker_L_to_N_MR1.class);
            blockerClass = ServiceBlocker_L_to_N_MR1.class;
        } else if (sdk == Build.VERSION_CODES.O) return false;
        else if (sdk == Build.VERSION_CODES.O_MR1) return false;
        else if (sdk == Build.VERSION_CODES.P) return false;
        else if (sdk == Build.VERSION_CODES.Q) return false;
        else return false;

        Toolbox.FastLog("INITIATING BLOCKER");
        if (!isServiceRunning(blockerClass, context)) context.startService(blockerIntent);

        return true;
    }

    private static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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