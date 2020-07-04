package com.andrewlevada.carephone.ui.extra;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.blockers.BlockerAccesser;
import com.andrewlevada.carephone.ui.ContactDevActivity;
import com.andrewlevada.carephone.ui.HelloActivity;
import com.andrewlevada.carephone.ui.TutorialActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class CommonSettings {
    public static void showTutorial(Activity activity, int userType) {
        Intent intent = new Intent(activity, TutorialActivity.class);
        intent.putExtra(TutorialActivity.INTENT_USER_TYPE, userType);
        activity.startActivity(intent);
    }


    public static void contactDev(Activity activity) {
        activity.startActivity(new Intent(activity, ContactDevActivity.class));
    }

    public static void switchActivityToHello(Activity activity) {
        BlockerAccesser.stop();
        Intent intent = new Intent(activity, HelloActivity.class);
        intent.putExtra(HelloActivity.INTENT_EXTRA_STAY, true);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void logout(Activity activity) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        BlockerAccesser.stop();
        FirebaseAuth.getInstance().signOut();
        WhitelistAccesser.getInstance().clearData();
        activity.finish();
    }

    public static void showThanksDialog(Context context) {
        Toolbox.showSimpleDialog(context,
                R.string.cared_settings_about_dialog_title,
                R.string.cared_settings_about_dialog_message,
                R.string.general_great);
    }

    public static void gotoDonateWebPage(Context context) {
        String url = FirebaseRemoteConfig.getInstance()
                .getString(Config.Analytics.remoteConfigDonateLink);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));

        try {
            context.startActivity(i);
        } catch (Exception e) {
            Toolbox.showSimpleDialog(context,
                    R.string.general_oh_oh,
                    R.string.settings_dialog_no_browser);
        }
    }
}
