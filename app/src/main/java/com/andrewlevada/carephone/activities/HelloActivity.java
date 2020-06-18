package com.andrewlevada.carephone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class HelloActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_STAY = "INTENT_EXTRA_STAY";

    private FirebaseAnalytics firebaseAnalytics;
    private boolean isStayState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        isStayState = getIntent().getBooleanExtra(INTENT_EXTRA_STAY, false);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Switch to other activity if user is authed
        if (FirebaseAuth.getInstance().getCurrentUser() != null && !isStayState) {
            int userType = getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE).getInt(AuthActivity.PARAM_NAME, -1);

            if (userType != -1) {
                FirebaseCrashlytics.getInstance().setCustomKey("auth_redirect", userType);

                Intent intent = null;

                if (userType == AuthActivity.TYPE_CARED) intent = new Intent(HelloActivity.this, HomeActivity.class);
                if (userType == AuthActivity.TYPE_CARETAKER) intent = new Intent(HelloActivity.this, CaretakerListActivity.class);

                startActivity(intent);
                finish();
            }
        }

        // Process buttons
        findViewById(R.id.button_cared).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) switchToAuth(AuthActivity.TYPE_CARED);
            else switchTo(HomeActivity.class, AuthActivity.TYPE_CARED);
        });
        findViewById(R.id.button_caretaker).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) switchToAuth(AuthActivity.TYPE_CARETAKER);
            else switchTo(CaretakerListActivity.class, AuthActivity.TYPE_CARETAKER);
        });
    }

    private void switchToAuth(int type) {
        Intent intent = new Intent(HelloActivity.this, AuthActivity.class);
        intent.putExtra(AuthActivity.PARAM_NAME, type);
        startActivity(intent);
        finish();
    }

    private void switchTo(Class<?> activity, int userType) {
        if (isStayState) getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE)
                .edit().putInt(AuthActivity.PARAM_NAME, userType).apply();

        Intent intent = new Intent(HelloActivity.this, activity);
        startActivity(intent);
        finish();
    }
}
