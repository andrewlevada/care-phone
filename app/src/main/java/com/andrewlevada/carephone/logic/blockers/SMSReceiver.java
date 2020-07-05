package com.andrewlevada.carephone.logic.blockers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.SyncSmsSender;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.ui.AuthActivity;
import com.andrewlevada.carephone.ui.HelloActivity;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

@SuppressLint("HandlerLeak")
public class SMSReceiver {
    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context.getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE)
                    .getInt(HelloActivity.PREFS_USER_TYPE, -1) != AuthActivity.TYPE_CARED) return;

            final Bundle bundle = intent.getExtras();
            try {
                if (bundle == null) return;

                final Object[] pdusArray = (Object[]) bundle.get("pdus");
                if (pdusArray == null) return;

                if (pdusArray.length == 0) return;
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdusArray[0]);

                if (sms == null) return;
                String text = sms.getDisplayMessageBody();

                Toolbox.fastLog("NEW SMS: num: " + sms.getDisplayOriginatingAddress() + "; msg: " + text);

                // TODO: Fix handler leak
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        WhitelistAccesser.getInstance().loadSyncSms(context, text);
                    }
                };

                Toolbox.InternetConnectionChecker.getInstance().hasInternet(hasInternet -> {
                    if (!hasInternet && isSyncSms(text)) handler.sendEmptyMessage(1);
                });

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Toolbox.fastLog(e.getMessage());
            }
        }
    }

//    public static void deleteSMS(Context ctx, String message, String number) {
//        try {
//            Cursor c = ctx.getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI,
//                    new String[] { "_id", "thread_id", "address",
//                            "person", "date", "body" }, null, null, null);
//
//            Toolbox.fastLog("count "+c.getCount() + " from " + Telephony.Sms.Inbox.CONTENT_URI);
//            if (c != null && c.moveToFirst()) {
//                do {
//                    long id = c.getLong(0);
//                    long threadId = c.getLong(1);
//                    String address = c.getString(2);
//                    String body = c.getString(5);
//                    Toolbox.fastLog("0>" + c.getString(0) + "1>" + c.getString(1)   + "2>" + c.getString(2) + "<-1>"  + c.getString(3) + "4>" + c.getString(4)+ "5>" + c.getString(5));
//
////                    if (body.contains(getResources().getText(R.string.invite_text).toString()) && address.equals(number)) {
//                    if (message.equals(body) && address.equals(number)) {
//                        // mLogger.logInfo("Deleting SMS with id: " + threadId);
//                        int rows = ctx.getContentResolver().delete(Telephony.Sms.Inbox.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build(), null, null);
//                        Toolbox.fastLog("Delete success......... rows: "+rows);
//                        Toolbox.fastLog("Delete success......... body: "+body);
//                        break;
//                    }
//                } while (c.moveToNext());
//            }
//
//        } catch (Exception e) {
//            Toolbox.fastLog(e.getMessage());
//        }
//    }

    private static boolean isSyncSms(String message) {
        return message.startsWith(SyncSmsSender.smsPrefix);
    }
}





















