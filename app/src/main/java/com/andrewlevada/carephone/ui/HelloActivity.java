package com.andrewlevada.carephone.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class HelloActivity extends AppCompatActivity {
    public static final String PREFS_USER_TYPE = "user_type";
    public static final String INTENT_EXTRA_STAY = "INTENT_EXTRA_STAY";

    private boolean isStayState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        isStayState = getIntent().getBooleanExtra(INTENT_EXTRA_STAY, false);

        Network.config().init(getApplicationContext());

        // Find views by ids
        Button caredButton = findViewById(R.id.button_cared);
        Button caretakerButton = findViewById(R.id.button_caretaker);

        // Check internet connection
        Toolbox.InternetConnectionChecker.getInstance().hasInternet(hasInternet -> {
            if (hasInternet) authCheck();
            else switchTo(HomeActivity.class, AuthActivity.TYPE_CARED);
        });

        // Firebase Remote Config
        Toolbox.setupFirebaseRemoteConfig();

        // Process buttons
        caredButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) switchToAuth(AuthActivity.TYPE_CARED);
            else switchTo(HomeActivity.class, AuthActivity.TYPE_CARED);
        });

        caretakerButton.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) switchToAuth(AuthActivity.TYPE_CARETAKER);
            else switchTo(CaretakerListActivity.class, AuthActivity.TYPE_CARETAKER);
        });
    }

    private void authCheck() {
        // Switch to other activity if user is authed
        if (FirebaseAuth.getInstance().getCurrentUser() != null && !isStayState) {
            int userType = getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE).getInt(PREFS_USER_TYPE, -1);

            if (userType != -1) {
                FirebaseCrashlytics.getInstance().setCustomKey("auth_redirect", userType);

                Intent intent = null;

                if (userType == AuthActivity.TYPE_CARED) intent = new Intent(HelloActivity.this, HomeActivity.class);
                if (userType == AuthActivity.TYPE_CARETAKER) intent = new Intent(HelloActivity.this, CaretakerListActivity.class);

                startActivity(intent);
                finish();
            }
        }

    }

    private void switchToAuth(int type) {
        getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE)
                .edit().putInt(PREFS_USER_TYPE, type).apply();

        if (Toolbox.isFirstUserTypeOpen(this, type)) {
            Intent intent = new Intent(HelloActivity.this, TutorialActivity.class);
            intent.putExtra(TutorialActivity.INTENT_USER_TYPE, type);
            intent.putExtra(TutorialActivity.INTENT_NEXT_ACTIVITY, AuthActivity.class.getName());
            startActivity(intent);
        } else {
            Intent intent = new Intent(HelloActivity.this, AuthActivity.class);
            startActivity(intent);
        }
    }

    private void switchTo(Class<?> activity, int userType) {
        if (isStayState) getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE)
                .edit().putInt(PREFS_USER_TYPE, userType).apply();

        Intent intent = new Intent(HelloActivity.this, activity);
        startActivity(intent);
        finish();
    }
}
