package com.andrewlevada.carephone.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andrewlevada.carephone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {
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
    private PhoneAuthProvider.ForceResendingToken token;
    private PhoneAuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Get state from intent
        userType = getIntent().getIntExtra(PARAM_NAME, TYPE_CARED);
        state = STATE_PHONE;

        // Setup auth
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("ru");
        authCallback = new AuthCallback(this);

        // Find views by ids
        infoTextView = findViewById(R.id.auth_info_text);
        button = findViewById(R.id.auth_button);
        editText = findViewById(R.id.auth_edit_text);

        // Process button onclick
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == STATE_PHONE) requestCodeSending();
                else if (state == STATE_CODE) processEnteredCode();
            }
        });
    }

    private void requestCodeSending() {
        if (editText.getText() == null) {
            editText.setError(getText(R.string.auth_enter_phone_number));
            return;
        }

        String phoneNumber = editText.getText().toString();
        int timeoutSeconds = 120;

        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(phoneNumber, timeoutSeconds, TimeUnit.SECONDS, this, authCallback);

        button.setActivated(false);
    }

    private void processEnteredCode() {
        if (editText.getText() == null) {
            editText.setError(getText(R.string.auth_enter_code));
            return;
        }

        if (verificationId == null) return;

        credential = PhoneAuthProvider.getCredential(verificationId, editText.getText().toString());
        auth();
    }

    private void auth() {
        Task<AuthResult> task = auth.signInWithCredential(credential);

        task.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = task.getResult().getUser();
                    continueToHomeActivity(user);
                } else {
                    editText.setError(getText(R.string.auth_wrong_code));
                }
            }
        });
    }

    private void continueToHomeActivity(FirebaseUser user) {
        Log.e("AUTH_APP", "DONE: " + user.getPhoneNumber());
    }

    private void onCodeSent() {
        state = STATE_CODE;

        // Setup editText
        editText.setText("");
        editText.setHint(getText(R.string.auth_code));

        // Setup button
        animateButton();
        button.setActivated(true);
        button.setText(getText(R.string.auth_check_code));

        // Setup text
        infoTextView.setText(getText(R.string.auth_info_second));
    }

    private void onInvalidPhoneNumber() {
        editText.setError(getText(R.string.auth_wrong_phone_number));
    }

    private void animateButton() {
        ObjectAnimator backgroundAnimation = ObjectAnimator.ofArgb(button, "backgroundColor",
                ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                ContextCompat.getColor(getApplicationContext(), R.color.colorSurface));
        backgroundAnimation.setDuration(600);

        ObjectAnimator textAnimation = ObjectAnimator.ofArgb(button, "textColor",
                ContextCompat.getColor(getApplicationContext(), R.color.colorOnPrimary),
                ContextCompat.getColor(getApplicationContext(), R.color.colorOnSurface));
        textAnimation.setDuration(600);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(backgroundAnimation).with(textAnimation);
        animatorSet.start();
    }

    private class AuthCallback extends PhoneAuthProvider.OnVerificationStateChangedCallbacks {
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

            Log.e("AUTH_APP", "wrong");
            e.printStackTrace();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            activity.verificationId = verificationId;
            activity.token = token;
            activity.onCodeSent();
        }

        AuthCallback(AuthActivity activity) {
            this.activity = activity;
        }
    }
}
