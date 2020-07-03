package com.andrewlevada.carephone;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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

    private static final String LOG_TAG = "CAREPHONE";
    private static final boolean DOLOG = true;

    private static final String PREFS_IS_FIRST_LAUNCH = "is_first_launch";

    @Nullable
    public static View getLastChild(@NonNull ViewGroup parent) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);

            if (child != null) return child;
        }
        return null;
    }

    public static void fastLog(String value) {
        if (DOLOG) Log.e(LOG_TAG, value);
    }

    public static String getShortStringFromSeconds(int seconds) {
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

        return seconds + " " + label;
    }

    public static String getShortStringFromMinutes(int minutes) {
        String label;
        if (minutes < 60) {
            label = "м";
        } else {
            minutes /= 60;
            label = "ч";
        }

        return minutes + " " + label;
    }

    public static String processPhone(String phone) {
        if (phone.length() > 1 && phone.substring(0, 1).equals("8")) {
            phone = "+7" + phone.substring(1);
        }

        return phone.replaceAll("\\s","").toLowerCase();
    }

    public static void requestFirebaseAuthToken(final AuthTokenCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) user.getIdToken(true)
                .addOnCompleteListener(task -> callback.onGenerated(task.getResult().getToken()))
                .addOnCanceledListener(() -> callback.onGenerated(null));
    }
    public interface AuthTokenCallback {
        void onGenerated(String token);
    }

    public static void putInClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    public static <T> String arrayToString(T[] array) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < array.length; i++) string.append(array[i].toString()).append(i == array.length - 1 ? ", " : "");
        return string.toString();
    }

    public static boolean isFirstUserTypeOpen(Context context, int userType) {
        SharedPreferences preferences = context
                .getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);

        boolean result = preferences.getBoolean(PREFS_IS_FIRST_LAUNCH + userType, true);
        preferences.edit().putBoolean(PREFS_IS_FIRST_LAUNCH + userType, false).apply();
        return result;
    }

    // Dialogs

    public static void showErrorDialog(Context context) {
        showSimpleDialog(context, R.string.general_oh_oh,
                R.string.general_something_wrong, R.string.general_terrible);
    }

    public static void showSimpleDialog(Context context, @StringRes int title, @StringRes int body) {
       showSimpleDialog(context, title, body, R.string.general_okay);
    }

    public static void showSimpleDialog(Context context, @StringRes int title, @StringRes int body,
                                        @StringRes int buttonText) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(buttonText, (dialog, which) -> {})
                .show();
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
                    Thread.sleep(2500);
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

    // Internet connection check

    public static class InternetConnectionChecker {
        private static InternetConnectionChecker instance;

        private boolean hadInternet;

        public void hasInternet(CallbackOne<Boolean> callback) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        int timeoutMs = 1500;
                        Socket sock = new Socket();
                        SocketAddress address = new InetSocketAddress("8.8.8.8", 53);

                        sock.connect(address, timeoutMs);
                        sock.close();

                        hadInternet = true;
                        callback.invoke(true);
                    } catch (IOException e) {
                        hadInternet = false;
                        callback.invoke(false);
                    }
                }
            };
            thread.start();
        }

        public boolean hasInternetSync() {
            hasInternet(hasInternet -> hadInternet = hasInternet);
            return hadInternet;
        }

        public static InternetConnectionChecker getInstance() {
            if (instance == null) instance = new InternetConnectionChecker();
            return instance;
        }
    }
}
