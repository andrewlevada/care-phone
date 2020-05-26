package com.andrewlevada.carephone.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.logic.blockers.Blocker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private Blocker blocker;
    private int currentHomeFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find views by ids
        BottomNavigationView navigation = findViewById(R.id.home_bottom_navigation);

        // Loading default learn fragment screen
        loadHomeFragment(new WhitelistFragment(), R.id.home_nav_list);

        // Process bottom navigation buttons clicks
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (currentHomeFragmentId == itemId) return false;

                switch (itemId) {
                    case R.id.home_nav_first:
                        // TODO: Add new fragment
                        break;

                    case R.id.home_nav_list:
                        fragment = new WhitelistFragment();
                        break;

                    case R.id.home_nav_stats:
                        // TODO: Add new fragment
                        break;
                }

                return loadHomeFragment(fragment, itemId);
            }
        });

        // Check for permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE};
                requestPermissions(permissions, 1); // TODO: Change 1 for constant and process
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.READ_PHONE_NUMBERS};
                requestPermissions(permissions, 2); // TODO: Change 2 for constant and process
            }
        }

        // Load whitelist blocker
        blocker = Blocker.getSuitableVersion(getApplicationContext());
        if (blocker == null) {
        } // TODO: Process phone with wrong api
        else blocker.initiateBlocking();
    }

    private boolean loadHomeFragment(Fragment fragment, int id) {
        if (fragment == null) return false;

        // Remember switching fragment
        currentHomeFragmentId = id;

        // Make transition between fragments
        FragmentTransaction transition = getSupportFragmentManager().beginTransaction();
        transition.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        transition.replace(R.id.home_fragment_container, fragment);
        transition.commit();

        return true;
    }

    @Override
    protected void onDestroy() {
        blocker.onDestroy();
        super.onDestroy();
    }
}