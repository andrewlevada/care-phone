package com.andrewlevada.carephone.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.logic.network.Network;

public class ContactDevActivity extends AppCompatActivity {
    private static final int maxMessageLength = 4000;

    private AutoCompleteTextView subjectTextView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_dev);

        // Find views by ids
        subjectTextView = findViewById(R.id.text_subject);
        editText = findViewById(R.id.edit_text);

        // Setup auto complete text view
        String[] subjects = getResources().getStringArray(R.array.contact_dev_subjects);
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, subjects);
        subjectTextView.setAdapter(subjectsAdapter);
        subjectTextView.setInputType(InputType.TYPE_NULL);

        // Process send onclick
        findViewById(R.id.send_button).setOnClickListener(view -> trySend());
    }

    private void trySend() {
        Editable subjectEditable = subjectTextView.getText();
        Editable messageEditable = editText.getText();

        if (subjectEditable.length() == 0) {
            subjectTextView.setError(getString(R.string.contact_dev_select_subject));
            return;
        }

        if (messageEditable.length() == 0) {
            editText.setError(getString(R.string.contact_dev_enter_text));
            return;
        } else if (messageEditable.length() > maxMessageLength) {
            editText.setError(getString(R.string.contact_dev_wrong_text_length));
            return;
        }

        Network.router().sendBugReport(subjectEditable.toString(), messageEditable.toString(), new Network.NetworkCallbackZero() {
            @Override
            public void onSuccess() {
                showSentBanner();
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                editText.setError(getString(R.string.general_something_wrong));
            }
        });
    }

    private void showSentBanner() {
        View sentBanner = findViewById(R.id.sent_layout);
        sentBanner.setVisibility(View.VISIBLE);

        ObjectAnimator backgroundAnimation = ObjectAnimator.ofFloat(sentBanner, "alpha", 0f, 1f);
        backgroundAnimation.setDuration(600);
        backgroundAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                new Handler().postDelayed(() -> finish(), 2000);
            }
        });

        backgroundAnimation.start();
    }
}
