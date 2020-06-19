package com.andrewlevada.carephone.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.network.Network;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {
    private static final String PREF_FAILED_ATTEMPTS = "auth_failed_attempts";
    private static final String ANALYTICS_FAILED_ATTEMPTS = "failed_attempts";

    public static final String PARAM_NAME = "user_type";
    public static final int TYPE_CARED = 0;
    public static final int TYPE_CARETAKER = 1;

    private static final int STATE_PHONE = 0;
    private static final int STATE_CODE = 1;

    private int userType;
    private int state;

    private TextView infoTextView;
    private Button button;
    private EditText editText;

    private FirebaseAuth auth;
    private AuthCallback authCallback;
    private String verificationId;
    private PhoneAuthCredential credential;

    private AuthButtonAppearanceController authButtonController;

    private boolean isNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authButtonController = new AuthButtonAppearanceController();

        // Get state from intent
        userType = getIntent().getIntExtra(PARAM_NAME, TYPE_CARED);
        state = STATE_PHONE;

        // Setup auth
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("ru");
        authCallback = new AuthCallback(this);

        // Find views by ids
        infoTextView = findViewById(R.id.info_text);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.edit_text);

        // Process button onclick
        button.setOnClickListener(v -> {
            if (state == STATE_PHONE) requestCodeSending();
            else if (state == STATE_CODE) processEnteredCode();
        });
    }

    private void requestCodeSending() {
        if (editText.getText() == null || editText.getText().length() == 0) {
            editText.setError(getText(R.string.general_enter_phone));
            return;
        }

        String phoneNumber = Toolbox.processPhone(editText.getText().toString());
        int timeoutSeconds = 120;

        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(phoneNumber, timeoutSeconds, TimeUnit.SECONDS, this, authCallback);

        authButtonController.onCodeRequested();
    }

    private void processEnteredCode() {
        if (editText.getText() == null || editText.getText().length() == 0) {
            editText.setError(getText(R.string.auth_enter_code));
            return;
        }

        if (verificationId == null) return;

        credential = PhoneAuthProvider.getCredential(verificationId, editText.getText().toString());
        auth();
    }

    private void auth() {
        Task<AuthResult> authTask = auth.signInWithCredential(credential);

        authTask.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                if (task.getResult() == null) {
                    onUnprocessedError();
                    return;
                }

                FirebaseUser user = task.getResult().getUser();

                if (user == null) {
                    onUnprocessedError();
                    return;
                }

                Network.config().useFirebaseAuthToken();
                Network.cared().addUserIfNew(null);

                if (task.getResult().getAdditionalUserInfo() != null)
                    isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                continueToNextActivity(user);
            } else {
                editText.setError(getText(R.string.auth_wrong_code));
            }
        });
    }

    private void continueToNextActivity(FirebaseUser user) {
        SharedPreferences prefs = getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);

        // Auth analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, user.getProviderId());
        bundle.putInt(ANALYTICS_FAILED_ATTEMPTS, prefs.getInt(PREF_FAILED_ATTEMPTS, 0));
        FirebaseAnalytics.getInstance(this).logEvent(
                isNewUser ? FirebaseAnalytics.Event.SIGN_UP : FirebaseAnalytics.Event.LOGIN, bundle);

        // Reset failed attempts counter
        prefs.edit().putInt(PREF_FAILED_ATTEMPTS, 0).apply();

        getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE)
                .edit().putInt(PARAM_NAME, userType).apply();

        // Move to next activity
        Intent intent = null;
        if (userType == TYPE_CARED) intent = new Intent(AuthActivity.this, HomeActivity.class);
        else if (userType == TYPE_CARETAKER) intent = new Intent(AuthActivity.this, CaretakerListActivity.class);
        startActivity(intent);
        finish();
    }

    private void onCodeSent() {
        state = STATE_CODE;

        // Setup editText
        editText.setText("");
        editText.setHint(getText(R.string.auth_code));

        // Setup button
        authButtonController.onCodeSent();

        // Setup text
        infoTextView.setText(getText(R.string.auth_info_second));
    }

    private void onInvalidPhoneNumber() {
        editText.setError(getText(R.string.general_wrong_phone));
    }

    private void onUnprocessedError() {
        editText.setText("");
        editText.setError(getString(R.string.general_something_wrong));
    }

    private static class AuthCallback extends PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        private AuthActivity activity;

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            activity.credential = phoneAuthCredential;
            activity.auth();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            if (e instanceof FirebaseAuthInvalidCredentialsException)
                activity.onInvalidPhoneNumber();

            // Save number of failed tries for analytics
            SharedPreferences prefs =
                    activity.getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);
            int amount = prefs.getInt(PREF_FAILED_ATTEMPTS, 0);
            prefs.edit().putInt(PREF_FAILED_ATTEMPTS, amount + 1).apply();

            e.printStackTrace();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            activity.verificationId = verificationId;
            activity.onCodeSent();
        }

        AuthCallback(AuthActivity activity) {
            this.activity = activity;
        }
    }

    private class AuthButtonAppearanceController {
        private void onCodeRequested() {
            button.setActivated(false);

            ObjectAnimator backgroundAnimation = ObjectAnimator.ofArgb(button, "backgroundColor",
                    ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                    ContextCompat.getColor(getApplicationContext(), R.color.colorSurface));
            backgroundAnimation.setDuration(600);
            backgroundAnimation.start();
        }

        private void onCodeSent() {
            button.setActivated(true);
            button.setText(getText(R.string.auth_check_code));

            ObjectAnimator textAnimation = ObjectAnimator.ofArgb(button, "textColor",
                    ContextCompat.getColor(getApplicationContext(), R.color.colorOnPrimary),
                    ContextCompat.getColor(getApplicationContext(), R.color.colorOnSurface));
            textAnimation.setDuration(600);
            textAnimation.start();
        }

        private void onCodeFailedToSend() {
            button.setActivated(true);

            ObjectAnimator backgroundAnimation = ObjectAnimator.ofArgb(button, "backgroundColor",
                    ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                    ContextCompat.getColor(getApplicationContext(), R.color.colorSurface));
            backgroundAnimation.setDuration(600);
            backgroundAnimation.start();
        }

        private void onReturnToFirstStep() {
            button.setText(getText(R.string.general_enter_phone));

            ObjectAnimator backgroundAnimation = ObjectAnimator.ofArgb(button, "backgroundColor",
                    ContextCompat.getColor(getApplicationContext(), R.color.colorSurface),
                    ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            backgroundAnimation.setDuration(600);

            ObjectAnimator textAnimation = ObjectAnimator.ofArgb(button, "textColor",
                    ContextCompat.getColor(getApplicationContext(), R.color.colorOnSurface),
                    ContextCompat.getColor(getApplicationContext(), R.color.colorOnPrimary));
            textAnimation.setDuration(600);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(backgroundAnimation).with(textAnimation);
            animatorSet.start();
        }
    }
}
