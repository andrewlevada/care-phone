package com.andrewlevada.carephone.logic.blockers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class Blocker_N_MR1 extends Blocker {

    @Override
    boolean receivedCall(Context context) {
        return false;
    }

    void declineCall(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) return;

            @SuppressLint("SoonBlockedPrivateApi")
            Method m = telephonyManager.getClass().getDeclaredMethod("getITelephony");

            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);
            if (telephonyService == null) return;
            telephonyService.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void continueCall(Context context) {
//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        if (audioManager != null) audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
}
