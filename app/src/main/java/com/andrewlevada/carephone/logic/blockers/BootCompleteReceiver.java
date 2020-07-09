package com.andrewlevada.carephone.logic.blockers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.AuthActivity;
import com.andrewlevada.carephone.ui.HelloActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toolbox.fastLog("Boot blockers activation");

        int userType = context.getSharedPreferences(
                Config.appSharedPreferences, Context.MODE_PRIVATE)
                .getInt(HelloActivity.PREFS_USER_TYPE, -1);
        if (userType != AuthActivity.TYPE_CARED) return;


        FirebaseCrashlytics.getInstance().setCustomKey("boot_competed", true);

        Toolbox.setupFirebaseRemoteConfig();
        Toolbox.InternetConnectionChecker.getInstance().hasInternet(null);

        // Setup analytics
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseCrashlytics.getInstance().setUserId(userUid);
            FirebaseAnalytics.getInstance(context).setUserId(userUid);
        } else Toolbox.fastLog("Not authed");

        Network.config().init(context);

        WhitelistAccesser whitelistAccesser = WhitelistAccesser.getInstance();
        whitelistAccesser.initialize(context, false);
        whitelistAccesser.syncWhitelist();
        whitelistAccesser.syncWhitelistState();

        BlockerAccesser.enable(context);
    }
}