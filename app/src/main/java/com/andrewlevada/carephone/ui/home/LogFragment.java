package com.andrewlevada.carephone.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.extra.recycleradapters.RecyclerAdapter;
import com.andrewlevada.carephone.ui.extra.recycleradapters.RecyclerLogAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LogFragment extends Fragment {
    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;
    public static final int TYPE_BLOCKED = 2;
    public static final int numberPerLoad = 20;

    private View emptyView;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private List<LogRecord> logRecords;
    private int loadedNumber;
    private boolean isLoading;

    private HomeActivity parentingActivity;

    // Required fragment_tutorial_empty public constructor
    public LogFragment() { }

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
        emptyView = layout.findViewById(R.id.empty_view);
        recyclerView = layout.findViewById(R.id.recycler);

        ((LogScrollView) layout.findViewById(R.id.scroll_layout)).setBottomCallback(this::loadMoreRecords);

        setupRecyclerView();
        new Handler().postDelayed(this::loadMoreRecords, 100);

        return layout;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        layoutManager.setItemPrefetchEnabled(false);

        recyclerView.setLayoutManager(layoutManager);

        logRecords = new ArrayList<>();
        adapter = new RecyclerLogAdapter(recyclerView, logRecords);
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
                if (arg.size() != 0) {
                    loadedNumber += arg.size();
                    logRecords.addAll(arg);
                    adapter.notifyItemRangeInserted(loadedNumber - arg.size(), arg.size());
                } else if (loadedNumber == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                isLoading = false;
                // TODO: Process failure
            }
        });
    }
}