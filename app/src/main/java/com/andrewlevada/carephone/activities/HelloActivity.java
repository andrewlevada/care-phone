package com.andrewlevada.carephone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.google.firebase.auth.FirebaseAuth;

public class HelloActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_STAY = "INTENT_EXTRA_STAY";

    private boolean isStayState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        isStayState = getIntent().getBooleanExtra(INTENT_EXTRA_STAY, false);

        // Switch to other activity if user is authed
        if (FirebaseAuth.getInstance().getCurrentUser() != null && !isStayState) {
            Toolbox.fastLog("AUTH REDIRECT");

            int userType = getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE).getInt(AuthActivity.PARAM_NAME, -1);

            if (userType != -1) {
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
