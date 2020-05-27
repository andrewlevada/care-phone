package com.andrewlevada.carephone.logic.blockers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class Blocker_N_MR1 extends Blocker {
    private BroadcastReceiver receiver;

    @Override
    public void initiateBlocking() {
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CALL);
//        //intentFilter.addAction("PHONE_STATE");
//        receiver = new IncomingCallReceiver();
//        context.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
//        if (receiver != null) {
//            context.unregisterReceiver(receiver);
//            receiver = null;
//        }
    }

    public static class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Toolbox.FastLog("INCOMING CALL DETECTING");

                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                Toolbox.FastLog("INCOMING CALL DETECTED: " + state);

                if (state == null || state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
                    return;

                TelephonyManager telephonyManager =
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                @SuppressLint("SoonBlockedPrivateApi")
                Method m = telephonyManager.getClass().getDeclaredMethod("getITelephony");

                m.setAccessible(true);
                ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);

                Toolbox.FastLog("DONE");
                telephonyService.endCall();
//                if (number == null || !WhitelistAccesser.isInList(number)) {
//                    telephonyService.endCall();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Blocker_N_MR1(Context context) {
        super(context);
    }
}
