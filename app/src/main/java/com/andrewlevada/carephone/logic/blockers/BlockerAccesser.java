package com.andrewlevada.carephone.logic.blockers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class BlockerAccesser {

    public static boolean enable(Context context) {
        Class<?> blockerClass;
        int sdk = Build.VERSION.SDK_INT;

        if (sdk == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN_MR1) return false;
        else if (sdk == Build.VERSION_CODES.JELLY_BEAN_MR2) return false;
        else if (sdk == Build.VERSION_CODES.KITKAT) return false;
        else if (sdk == Build.VERSION_CODES.LOLLIPOP) blockerClass = Blocker_L_to_N_MR1.class;
        else if (sdk == Build.VERSION_CODES.LOLLIPOP_MR1) blockerClass = Blocker_L_to_N_MR1.class;
        else if (sdk == Build.VERSION_CODES.M) blockerClass = Blocker_L_to_N_MR1.class;
        else if (sdk == Build.VERSION_CODES.N) blockerClass = Blocker_L_to_N_MR1.class;
        else if (sdk == Build.VERSION_CODES.N_MR1) blockerClass = Blocker_L_to_N_MR1.class;
        else if (sdk == Build.VERSION_CODES.O) blockerClass = Blocker_P.class;
        else if (sdk == Build.VERSION_CODES.O_MR1) blockerClass = Blocker_P.class;
        else if (sdk == Build.VERSION_CODES.P) blockerClass = Blocker_P.class;
        else if (sdk == Build.VERSION_CODES.Q) blockerClass = Blocker_P.class;
        else return false;

        FirebaseCrashlytics.getInstance().setCustomKey("blocker_type", blockerClass.getName());
        FirebaseAnalytics.getInstance(context).setUserProperty(
                Config.Analytics.userPropertyBlockerType, blockerClass.getName());

        Toolbox.fastLog("INIT BLOCKER?");

        if (!isServiceRunning(blockerClass, context)) {
            Toolbox.fastLog("YES!");
            FirebaseCrashlytics.getInstance().setCustomKey("did_blocker_init", true);

            context.startService(new Intent(context, blockerClass));
            //if (sdk < Build.VERSION_CODES.O) context.startService(new Intent(context, blockerClass));
            //else context.startForegroundService(new Intent(context, blockerClass));
        }

        return true;
    }

    public static void stop() {
        Blocker_L_to_N_MR1.tryStop();
        Blocker_O.tryStop();
        Blocker_P.tryStop();
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
}