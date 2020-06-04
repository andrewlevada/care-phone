package com.andrewlevada.carephone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.andrewlevada.carephone.LogFragment;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.BackdropActivity;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.blockers.Blocker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends BackdropActivity {
    private int currentHomeFragmentId;

    private FloatingActionButton fabView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.activity_home;
        layoutBackdropId = R.layout.activity_home_backdrop;
        super.onCreate(savedInstanceState);

        // Find views by ids
        BottomNavigationView navigation = findViewById(R.id.home_bottom_navigation);
        fabView = findViewById(R.id.home_fab);

        // Loading default fragment screen
        loadHomeFragment(new WhitelistFragment(this), R.id.home_nav_list);
        navigation.setSelectedItemId(R.id.home_nav_list);

        // Process bottom navigation buttons clicks
        final HomeActivity itself = this;
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (currentHomeFragmentId == itemId) return false;

                switch (itemId) {
                    case R.id.home_nav_log:
                        fragment = new LogFragment();
                        break;

                    case R.id.home_nav_list:
                        fragment = new WhitelistFragment(itself);
                        break;

                    case R.id.home_nav_stats:
                        fragment = new StatisticsFragment();
                        break;
                }

                return loadHomeFragment(fragment, itemId);
            }
        });

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
        WhitelistAccesser.getInstance().initialize(getApplicationContext());
        if (!Blocker.enable(getApplicationContext())) {
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
        transition.replace(R.id.home_fragment_container, fragment);
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
}