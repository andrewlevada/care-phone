package com.andrewlevada.carephone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.CloudActivity;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.blockers.Blocker;
import com.andrewlevada.carephone.logic.network.Network;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class HomeActivity extends CloudActivity {
    public static final String INTENT_REMOTE = "INTENT_REMOTE";

    private int currentHomeFragmentId;
    boolean isRemote;

    private FloatingActionButton fabView;
    public boolean doCloseLinkOnCloudCollapse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.activity_home;
        layoutCloudId = R.layout.activity_home_cloud;
        super.onCreate(savedInstanceState);

        // Analytics
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseCrashlytics.getInstance().setUserId(userUid);
        FirebaseAnalytics.getInstance(this).setUserId(userUid);

        // Get remote option from intent
        isRemote = getIntent().getBooleanExtra(INTENT_REMOTE, false);

        // Find views by ids
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        fabView = findViewById(R.id.fab);

        // Loading default fragment screen
        loadHomeFragment(new WhitelistFragment(this), R.id.home_nav_list);
        navigation.setSelectedItemId(R.id.home_nav_list);

        // Process bottom navigation buttons clicks
        final HomeActivity itself = this;
        navigation.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (currentHomeFragmentId == itemId) return false;

            switch (itemId) {
                case R.id.home_nav_log:
                    fragment = new LogFragment(itself);
                    break;

                case R.id.home_nav_list:
                    fragment = new WhitelistFragment(itself);
                    break;

                case R.id.home_nav_stats:
                    fragment = new StatisticsFragment(itself);
                    break;
            }

            return loadHomeFragment(fragment, itemId);
        });

        WhitelistAccesser.getInstance().initialize(getApplicationContext(), isRemote);

        if (isRemote) return;

        // Check for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE};
                requestPermissions(permissions, 1); // TODO: Change 1 for constant and process
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_PHONE_NUMBERS};
                requestPermissions(permissions, 2); // TODO: Change 2 for constant and process
            }
        }

        // Back button processing
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(HomeActivity.this, HelloActivity.class);
                intent.putExtra(HelloActivity.INTENT_EXTRA_STAY, true);
                startActivity(intent);
                finish();
            }
        });

        // Load whitelist blocker
        if (!Blocker.enable(getApplicationContext())) {
            FirebaseCrashlytics.getInstance().setCustomKey("blocker_type", "none");
            // TODO: Process unsupported device
        }
    }

    private boolean loadHomeFragment(Fragment fragment, int id) {
        if (fragment == null) return false;

        // Remember switching fragment
        currentHomeFragmentId = id;

        // Hide fab. If fragment needs it, it can request it
        fabView.hide();

        // Make transition between fragments
        FragmentTransaction transition = getSupportFragmentManager().beginTransaction();
        transition.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        transition.replace(R.id.fragment_container, fragment);
        transition.commit();

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
}