package com.andrewlevada.carephone;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    public static void fastLog(String value) {
        Log.e("TEST", value);
    }

    public static String intToHoursString(int num) {
        if (num == 0) return "Меньше часа";
        else if (num > 9999) return "Невероятно много";
        else if (num >= 11 && num <= 14) return num + " часов";
        else if (num % 10 == 1) return num + " час";
        else if (num % 10 >= 2 && num % 10 <= 4) return num + " часа";
        else return num + " часов";
    }

    public static String getShortStringFromTime(int seconds) {
        String label;
        if (seconds < 60) {
            label = "с";
        } else if (seconds < 60 * 60) {
            seconds /= 60;
            label = "м";
        } else {
            seconds /= 60 * 60;
            label = "ч";
        }

        return seconds + label;
    }

    public static String processPhone(String phone) {
        if (phone.length() > 1 && phone.substring(0, 1).equals("8")) {
            phone = "+7" + phone.substring(1);
        }

        return phone.replaceAll("\\s","").toLowerCase();
    }

    public interface AuthTokenCallback {
        void onGenerated(String token);
    }
    public static void requestFirebaseAuthToken(final AuthTokenCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) user.getIdToken(true).addOnCompleteListener(
                task -> callback.onGenerated(task.getResult().getToken()));
    }

    public static void putInClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    // Sync thread used for syncing data

    public interface InSyncThread {
        void sync();
    }
    public static SyncThread getSyncThread(Fragment fragment, InSyncThread call) {
        return new SyncThread(fragment, call);
    }
    public static class SyncThread extends Thread {
        public Fragment fragment;
        private InSyncThread call;

        @Override
        public void run() {
            while (fragment != null && fragment.isAdded()) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                call.sync();
            }
        }

        public SyncThread(Fragment fragment, InSyncThread call) {
            this.fragment = fragment;
            this.call = call;
        }
    }

    // Several general callbacks

    public interface Callback {
        void invoke();
    }
    public interface CallbackOne<T> {
        void invoke(T arg);
    }
    public interface CallbackState {
        void invoke();
        void fail();
    }
    public interface CallbackStateOne<T> {
        void invoke(T arg);
        void fail();
    }
}
