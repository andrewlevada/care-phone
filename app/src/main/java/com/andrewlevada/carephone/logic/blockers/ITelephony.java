package com.android.internal.telephony;

/**
 * This class is strange. But it is needed for blocking call.
 * It is supposed to be not in this apps package.
 */
public interface ITelephony {
    boolean endCall();
    void answerRingingCall();
    void silenceRinger();
}