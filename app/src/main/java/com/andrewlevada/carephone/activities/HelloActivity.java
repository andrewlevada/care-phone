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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        // Switch to other activity if user is authed
        if (FirebaseAuth.getInstance().getCurrentUser() != null && !getIntent().getBooleanExtra(INTENT_EXTRA_STAY, false)) {
            Toolbox.FastLog("AUTH REDIRECT");

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
        findViewById(R.id.hello_button_cared).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) switchToAuth(AuthActivity.TYPE_CARED);
            else switchTo(HomeActivity.class);
        });
        findViewById(R.id.hello_button_caretaker).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) switchToAuth(AuthActivity.TYPE_CARETAKER);
            else switchTo(CaretakerListActivity.class);
        });
    }

    private void switchToAuth(int type) {
        Intent intent = new Intent(HelloActivity.this, AuthActivity.class);
        intent.putExtra(AuthActivity.PARAM_NAME, type);
        startActivity(intent);
        finish();
    }

    private void switchTo(Class activity) {
        Intent intent = new Intent(HelloActivity.this, activity);
        startActivity(intent);
        finish();
    }
}
