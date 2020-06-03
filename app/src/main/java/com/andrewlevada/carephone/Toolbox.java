package com.andrewlevada.carephone;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

/**
 * This class contains all kinds of tools
 * that could be useful during development.
 */
public class Toolbox {
    /**
     * Finds the last child of given parent.
     * This child is ether last inflated or simply last in layout.
     *
     * @param parent {@link ViewGroup} which's child to find
     * @return Last child {@link View} or null if parent has no children.
     */
    @Nullable
    public static View getLastChild(@NonNull ViewGroup parent) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);

            if (child != null) return child;
        }
        return null;
    }

    public static void FastLog(String value) {
        Log.e("TEST", value);
    }

    public static void requestFirebaseAuthToken(final AuthTokenCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                callback.onGenerated(task.getResult().getToken());
            }
        });
    }

    public static String intToHoursString(int num) {
        if (num == 0) return "Меньше часа";
        else if (num > 9999) return "Невероятно много";
        else if (num >= 11 && num <= 14) return num + " часов";
        else if (num % 10 == 1) return num + " час";
        else if (num % 10 >= 2 && num % 10 <= 4) return num + " часа";
        else return num + " часов";
    }

    public interface AuthTokenCallback {
        void onGenerated(String token);
    }
}
