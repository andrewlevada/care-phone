package com.andrewlevada.carephone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HelloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        // Process buttons
        findViewById(R.id.hello_button_cared).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToAuth(AuthActivity.TYPE_CARED);
            }
        });

        findViewById(R.id.hello_button_caretaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToAuth(AuthActivity.TYPE_CARETAKER);
            }
        });
    }

    private void switchToAuth(int type) {
        Intent intent = new Intent(HelloActivity.this, AuthActivity.class);
        intent.putExtra(AuthActivity.PARAM_NAME, type);
        startActivity(intent);
        finish();
    }
}
