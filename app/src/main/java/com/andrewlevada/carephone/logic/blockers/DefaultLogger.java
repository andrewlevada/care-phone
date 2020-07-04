package com.andrewlevada.carephone.logic.blockers;

import androidx.annotation.Nullable;

import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.home.LogFragment;

import java.util.Date;

class DefaultLogger {
    private Date callStartTime;
    private int callType;

    public void onBlocked(@Nullable String phone) {
        Network.cared().addToLog(
                new LogRecord(phone == null ? "" : phone,
                        System.currentTimeMillis(), 0, LogFragment.TYPE_BLOCKED), null);
    }

    public void onIncomingStart() {
        callStartTime = new Date(System.currentTimeMillis());
        callType = LogFragment.TYPE_INCOMING;
    }

    public void onOutgoingStart() {
        callStartTime = new Date(System.currentTimeMillis());
        callType = LogFragment.TYPE_OUTGOING;
    }

    public void onCurrentEnd(String phone) {
        int duration = (int)((System.currentTimeMillis() - callStartTime.getTime()) / 1000);
        Network.cared().addToLog(new LogRecord(
                phone == null ? "" : phone, callStartTime.getTime(), duration, callType), null);
        callStartTime = null;
        callType = -1;
    }
}
