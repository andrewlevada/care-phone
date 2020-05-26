package com.andrewlevada.carephone.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.RecyclerNumberAdapter;
import com.andrewlevada.carephone.logic.WhitelistAccesser;

public class WhitelistFragment extends Fragment {
    private RecyclerView recyclerView;

    public WhitelistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate fragment view
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_whitelist, container, false);

        // Setup recycler view
        recyclerView = (RecyclerView) layout.findViewById(R.id.home_whitelist_recycler);
        setupRecyclerView();

        return layout;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerNumberAdapter adapter = new RecyclerNumberAdapter(recyclerView, WhitelistAccesser.whitelist, false);
        recyclerView.setAdapter(adapter);
    }
}
