package com.andrewlevada.carephone.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.activities.extra.CloudActivity;
import com.andrewlevada.carephone.activities.extra.CommonSettings;
import com.andrewlevada.carephone.activities.extra.RecyclerAdapter;
import com.andrewlevada.carephone.activities.extra.RecyclerOnlyPhoneAdapter;
import com.andrewlevada.carephone.logic.CaredUser;
import com.andrewlevada.carephone.logic.network.Network;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

public class CaretakerListActivity extends CloudActivity {
    private FirebaseAnalytics analytics;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private Toolbar toolbar;

    private List<CaredUser> cared;

    private CaretakerListActivity itself;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.activity_caretaker_list;
        layoutCloudId = R.layout.activity_caretaker_list_cloud;
        super.onCreate(savedInstanceState);
        itself = this;

        analytics = FirebaseAnalytics.getInstance(this);

        // Find views by ids
        final EditText codeEditText = findViewById(R.id.cloud_code);
        View resultButton = findViewById(R.id.cloud_result_button);
        toolbar = findViewById(R.id.list_toolbar);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycler);

        // Process fab onclick
        fab.setOnClickListener(v -> updateCloud(true));

        // Process toolbar actions
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.caretaker_list_settings) {
                inflateSettingsMenu();
                return true;
            }

            return false;
        });

        setupRecyclerView();
        syncCaredList();

        // Process cloud result button onclick
        resultButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString();

            if (code.length() != 6) {
                codeEditText.setError(getString(R.string.caretaker_list_wrong_code));
                return;
            }

            Network.caretaker().tryToLinkCaretaker(code, new Network.NetworkCallbackOne<Integer>() {
                @Override
                public void onSuccess(Integer resultCode) {
                    if (resultCode == 1) {
                        syncCaredList();
                        updateCloud(false);
                        analytics.logEvent(Config.Analytics.eventLinkUser, null);
                    } else
                        codeEditText.setError(getString(R.string.caretaker_list_wrong_code));
                }

                @Override
                public void onFailure(@Nullable Throwable throwable) {
                    codeEditText.setError(getString(R.string.general_something_wrong));
                }
            });
        });
    }

    private void syncCaredList() {
        Network.caretaker().getCaredList(new Network.NetworkCallbackOne<List<CaredUser>>() {
            @Override
            public void onSuccess(List<CaredUser> arg) {
                cared.clear();
                cared.addAll(arg);

                analytics.setUserProperty(Config.Analytics.userPropertyCaretakerListLength,
                        String.valueOf(arg.size()));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                // TODO: Process failure
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        cared = new ArrayList<>();
        adapter = new RecyclerOnlyPhoneAdapter(recyclerView, cared, index -> launchHomeActivityRemotely(cared.get(index).getUid()));
        recyclerView.setAdapter(adapter);
    }

    private void launchHomeActivityRemotely(String rUid) {
        Network.caretaker().rUid = rUid;

        Intent intent = new Intent(CaretakerListActivity.this, HomeActivity.class);
        intent.putExtra(HomeActivity.INTENT_REMOTE, true);
        startActivity(intent);
    }

    private void inflateSettingsMenu() {
        PopupMenu popupMenu;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            popupMenu = new PopupMenu(this, toolbar, Gravity.END | Gravity.TOP,
                    R.attr.popupMenuStyle, R.style.Widget_Custom_PopupMenu);
        else popupMenu = new PopupMenu(this, toolbar, Gravity.END | Gravity.TOP);

        popupMenu.getMenuInflater().inflate(R.menu.caretaker_settings, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new OnSettingsMenuItemClick());
    }

    @Override
    public void updateCloud(boolean extend) {
        super.updateCloud(extend);

        if (extend) fab.hide();
        else fab.show();
    }

    @Override
    public void fillCloud(int layout, @Nullable SimpleInflater.OnViewInflated callback, @Nullable View.OnClickListener resultOnClick) { }

    private class OnSettingsMenuItemClick implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.caretaker_settings_change_user_type)
                CommonSettings.switchActivityToHello(itself);
            else if (id == R.id.caretaker_settings_sign_out)
                CommonSettings.logout(itself);
            else if (id == R.id.caretaker_settings_thanks)
                CommonSettings.showThanksDialog(itself);
            else if (id == R.id.caretaker_settings_donate)
                CommonSettings.gotoDonateWebPage(itself);

            return true;
        }
    }
}