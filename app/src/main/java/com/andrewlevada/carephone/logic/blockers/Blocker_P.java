package com.andrewlevada.carephone.logic.blockers;

import android.content.Context;
import android.telecom.TelecomManager;

import androidx.annotation.RequiresApi;

import com.andrewlevada.carephone.Toolbox;

@RequiresApi(28)
class Blocker_P extends Blocker {

    @Override
    boolean receivedCall(Context context) {
        return true;
    }

    void declineCall(Context context) {
        Toolbox.FastLog("IN");
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager == null) Toolbox.FastLog("SR NL");
            else telecomManager.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void continueCall(Context context) {
//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        if (audioManager != null) audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
}
