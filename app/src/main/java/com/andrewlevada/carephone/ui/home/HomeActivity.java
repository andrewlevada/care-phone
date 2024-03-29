package com.andrewlevada.carephone.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.blockers.BlockerAccesser;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.HelloActivity;
import com.andrewlevada.carephone.ui.extra.CloudActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends CloudActivity {
    public static final String PREFS_OREO_WARNING = "oreo_warning";
    public static final String PREFS_ERROR_OCCURRED = "error_occurred";
    public static final String INTENT_REMOTE = "INTENT_REMOTE";

    private int currentHomeFragmentId;
    private FragmentIndex currentFragmentIndex;
    boolean isRemote;

    private FloatingActionButton fabView;
    public boolean doCloseLinkOnCloudCollapse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.activity_home;
        layoutCloudId = R.layout.activity_home_cloud;
        super.onCreate(savedInstanceState);

        // Check auth
        Toolbox.InternetConnectionChecker.getInstance().hasInternet(hasInternet -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                if (hasInternet) {
                    startActivity(new Intent(HomeActivity.this, HelloActivity.class));
                    finish();
                } else {
                    // NO INTERNET
                }

                return;
            }

            // Setup analytics
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseCrashlytics.getInstance().setUserId(userUid);
            FirebaseAnalytics.getInstance(this).setUserId(userUid);
        });

        // Get remote option from intent
        isRemote = getIntent().getBooleanExtra(INTENT_REMOTE, false);

        // Find views by ids
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        fabView = findViewById(R.id.fab);

        // Inflate correct menu
        if (isRemote) navigation.inflateMenu(R.menu.home_navigation_remote);
        else navigation.inflateMenu(R.menu.home_navigation);

        // Loading default fragment screen
        currentFragmentIndex = FragmentIndex.list;
        loadHomeFragment(new WhitelistFragment(this), R.id.home_nav_list, FragmentIndex.list);
        navigation.setSelectedItemId(R.id.home_nav_list);

        // Process bottom navigation buttons clicks
        final HomeActivity itself = this;
        navigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            FragmentIndex fragmentIndex;
            Fragment fragment;

            if (currentHomeFragmentId == itemId) return false;

            switch (itemId) {
                case R.id.home_nav_log: {
                    fragment = new LogFragment(itself);
                    fragmentIndex = FragmentIndex.log;
                    break;
                }

                case R.id.home_nav_list: {
                    fragment = new WhitelistFragment(itself);
                    fragmentIndex = FragmentIndex.list;
                    break;
                }

                case R.id.home_nav_stats: {
                    fragment = new StatisticsFragment(itself);
                    fragmentIndex = FragmentIndex.stats;
                    break;
                }

                case R.id.home_nav_settings: {
                    fragment = new SettingsFragment(itself);
                    fragmentIndex = FragmentIndex.settings;
                    break;
                }

                default: return false;
            }

            return loadHomeFragment(fragment, itemId, fragmentIndex);
        });

        WhitelistAccesser.getInstance().initialize(getApplicationContext(), isRemote);

        // Check if error have occurred
        SharedPreferences preferences =
                getSharedPreferences(Config.appSharedPreferences, MODE_PRIVATE);

        if (preferences.getBoolean(PREFS_ERROR_OCCURRED, false)) {
            Toolbox.showSimpleDialog(this,
                    R.string.general_oh_oh, R.string.other_dialog_error_occurred);

            preferences.edit().putBoolean(PREFS_ERROR_OCCURRED, false).apply();
        }
    }

    private boolean loadHomeFragment(Fragment fragment, int id, FragmentIndex fragmentIndex) {
        if (fragment == null) return false;

        // Remember switching fragment
        currentHomeFragmentId = id;
        boolean directionBool = currentFragmentIndex.compareTo(fragmentIndex) > 0;
        currentFragmentIndex = fragmentIndex;

        // Hide fab. If fragment needs it, it can request it
        fabView.hide();

        // Make transition between fragments
        FragmentTransaction transition = getSupportFragmentManager().beginTransaction();
        transition.setCustomAnimations(
                directionBool ? R.anim.float_in_left : R.anim.float_in_right,
                directionBool ? R.anim.float_out_right : R.anim.float_out_left);
        transition.replace(R.id.fragment_container, fragment);
        transition.commit();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isRemote) return;

        // Load whitelist blocker
        if (!checkPermissions()) return;
        tryToLaunchBlocker();
    }

    @SuppressLint("InlinedApi")
    private boolean checkPermissions() {
        int sdk = Build.VERSION.SDK_INT;

        List<String> requestedPermissions = new ArrayList<>();

        if (sdk >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                requestedPermissions.add(Manifest.permission.READ_PHONE_STATE);
                requestedPermissions.add(Manifest.permission.CALL_PHONE);
            }
        }

        if (sdk >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_DENIED) {
                requestedPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
                requestedPermissions.add(Manifest.permission.READ_PHONE_NUMBERS);
            }
        }

        if (requestedPermissions.size() != 0) {
            requestPermissions(requestedPermissions.toArray(new String[0]), 1);
            return false;
        }

        if (sdk == Build.VERSION_CODES.O || sdk == Build.VERSION_CODES.O_MR1) {
            String notificationListenerString = Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners");
            if (notificationListenerString == null || !notificationListenerString.contains(getPackageName())) {
                showBeforePermissionDialog(R.string.permission_dialog_notification_listener, () ->
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
                return false;
            }

            if (!getSharedPreferences(Config.appSharedPreferences, MODE_PRIVATE)
                    .getBoolean(PREFS_OREO_WARNING, false)) {
                getSharedPreferences(Config.appSharedPreferences, MODE_PRIVATE).edit()
                        .putBoolean(PREFS_OREO_WARNING, true).apply();
                Toolbox.showSimpleDialog(this,
                        R.string.general_warning,
                        R.string.other_dialog_oreo_warning);
            }
        }

//        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED) {
//            requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS }, 1);
//            return false;
//        }

        return true;
    }

    public void requestFAB(@Nullable View.OnClickListener onClickListener) {
        fabView.show();
        fabView.setOnClickListener(onClickListener);
    }

    public void hideFAB() {
        fabView.hide();
    }

    @Override
    public void updateCloud(boolean extend) {
        super.updateCloud(extend);

        if (!extend && doCloseLinkOnCloudCollapse && !isRemote) {
            Network.cared().removeLinkRequest(null);
            doCloseLinkOnCloudCollapse = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                showBeforePermissionDialog(R.string.permission_dialog_must_accept, this::checkPermissions);
                break;
            }
        }
    }

    private void tryToLaunchBlocker() {
        if (!BlockerAccesser.enable(getApplicationContext())) {
            FirebaseCrashlytics.getInstance().setCustomKey("blocker_type", "none");
            // TODO: Process unsupported device
        }
    }

    private void showBeforePermissionDialog(@StringRes int messageRes, Toolbox.Callback onClick) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.permission_dialog_title)
                .setMessage(messageRes)
                .setPositiveButton(R.string.general_okay, (dialog, which) -> onClick.invoke())
                .show();
    }

    private enum FragmentIndex {
        log,
        list,
        stats,
        settings
    }
}