package com.andrewlevada.carephone.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.RecyclerAdapter;
import com.andrewlevada.carephone.activities.extra.RecyclerLogAdapter;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.network.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LogFragment extends Fragment {
    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;
    private static final int numberPerLoad = 20;

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private List<LogRecord> logRecords;
    private int loadedNumber;
    private boolean isLoading;

    private HomeActivity parentingActivity;

    // Required empty public constructor
    public LogFragment() {

    }

    public LogFragment(HomeActivity parentingActivity) {
        this.parentingActivity = parentingActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_log, container, false);

        // Try to get parenting activity if not given
        if (parentingActivity == null && container.getContext() instanceof HomeActivity)
            parentingActivity = (HomeActivity) container.getContext();

        // Find views by ids
        recyclerView = layout.findViewById(R.id.recycler);

        setupRecyclerView();
        loadMoreRecords();

        return layout;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        logRecords = new ArrayList<>();
        adapter = new RecyclerLogAdapter(recyclerView, logRecords, this::loadMoreRecords);
        recyclerView.setAdapter(adapter);
    }

    private void loadMoreRecords() {
        if (isLoading) return;
        isLoading = true;

        Network.router().getLog(parentingActivity.isRemote, numberPerLoad, loadedNumber,
                new Network.NetworkCallbackOne<List<LogRecord>>() {
            @Override
            public void onSuccess(List<LogRecord> arg) {
                isLoading = false;
                loadedNumber += numberPerLoad;
                logRecords.addAll(arg);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                isLoading = false;
                // TODO: Process failure
            }
        });
    }
}