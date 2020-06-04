package com.andrewlevada.carephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.activities.extra.BackdropActivity;
import com.andrewlevada.carephone.activities.extra.RecyclerOnlyPhoneAdapter;
import com.andrewlevada.carephone.logic.network.Network;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CaretakerListActivity extends BackdropActivity {
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private List<String> cared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.activity_caretaker_list;
        layoutBackdropId = R.layout.activity_caretaker_list_backdrop;
        super.onCreate(savedInstanceState);

        // Find views by ids
        fab = findViewById(R.id.caretaker_list_fab);
        recyclerView = findViewById(R.id.caretaker_list_recycler);

        // Process fab onclick
        fab.setOnClickListener(v -> updateBackdrop(true));

        // Back button processing
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(CaretakerListActivity.this, HelloActivity.class);
                intent.putExtra(HelloActivity.INTENT_EXTRA_STAY, true);
                startActivity(intent);
                finish();
            }
        });

        setupRecyclerView();

        // Get cared list from server
        Network.getInstance().getCaredList(new Network.NetworkCallbackOne<List<String>>() {
            @Override
            public void onSuccess(List<String> arg) {
                cared.clear();
                cared.addAll(arg);
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
        adapter = new RecyclerOnlyPhoneAdapter(recyclerView, cared);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateBackdrop(boolean extend) {
        super.updateBackdrop(extend);

        if (extend) fab.hide();
        else fab.show();
    }

    @Override
    public void fillBackdrop(int layout, @Nullable SimpleInflater.OnViewInflated callback, @Nullable View.OnClickListener resultOnClick) { }
}