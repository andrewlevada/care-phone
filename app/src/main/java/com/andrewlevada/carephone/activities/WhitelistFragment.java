package com.andrewlevada.carephone.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.RecyclerNumberAdapter;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.WhitelistAccesser;

public class WhitelistFragment extends Fragment {
    private RecyclerView recyclerView;
    private ConstraintLayout layout;
    private View onclick;

    private ConstraintSet defaultConstraint;
    private ConstraintSet fullscreenConstraint;

    private HomeActivity parentingActivity;
    private RecyclerNumberAdapter adapter;

    private boolean isFullscreen;

    public WhitelistFragment() {
        // Required empty public constructor
    }

    WhitelistFragment(HomeActivity parentingActivity) {
        this.parentingActivity = parentingActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate fragment view
        layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_whitelist, container, false);

        // Get views by id
        Toolbar toolbar = layout.findViewById(R.id.home_whitelist_fullscreen_toolbar);
        onclick = layout.findViewById(R.id.home_whitelist_onclick);

        // Setup recycler view
        recyclerView = layout.findViewById(R.id.home_whitelist_recycler);
        setupRecyclerView();

        // Setup ConstraintSets for fullscreen animations
        defaultConstraint = new ConstraintSet();
        defaultConstraint.clone(layout);
        fullscreenConstraint = new ConstraintSet();
        fullscreenConstraint.load(container.getContext(), R.layout.fragment_whitelist_fullscreen);

        // Fill backdrop
        parentingActivity.fillBackdrop(R.layout.backdrop_content_whitelist_add, null, new OnBackdropResultClick());

        // Whitelist onclick processing
        onclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullscreen) return;
                updateFullscreen(true);
                // parentingActivity.updateFullscreen(true);
            }
        });

        // Fullscreen toolbar onclick processing
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullscreen) return;
                updateFullscreen(false);
                // parentingActivity.updateFullscreen(false);
            }
        });

        return layout;
    }

    private void updateFullscreen(boolean doExtend) {
        ConstraintSet constraintSet;

        isFullscreen = doExtend;
        onclick.setVisibility(doExtend ? View.GONE : View.VISIBLE);

        // Request fab
        if (doExtend) parentingActivity.requestFAB(new OnFABClick());
        else parentingActivity.hideFAB();

        // Update recycler view
        adapter.setExtended(doExtend);

        // Load needed layout
        if (doExtend) constraintSet = fullscreenConstraint;
        else constraintSet = defaultConstraint;

        // Setup transition
        Transition transition = new AutoTransition();
        transition.setDuration(600);
        if (doExtend) transition.setInterpolator(new FastOutSlowInInterpolator());
        else transition.setInterpolator(new FastOutSlowInInterpolator());

        // Make transition
        TransitionManager.beginDelayedTransition(layout, transition);
        constraintSet.applyTo(layout);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerNumberAdapter(recyclerView, WhitelistAccesser.whitelist);
        recyclerView.setAdapter(adapter);
    }

    private class OnFABClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            parentingActivity.updateBackdrop(true);
        }
    }

    private class OnBackdropResultClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ViewGroup parent = (ViewGroup) v.getParent();
            TextView labelView = parent.findViewById(R.id.backdrop_content_text_name);
            String label = labelView.getText().toString();
            TextView phoneView = parent.findViewById(R.id.backdrop_content_text_phone);
            String phone = phoneView.getText().toString();

            // TODO: Add more checks to data and highlight error fields
            if (label.equals("") || phone.equals("")) return;

            WhitelistAccesser.whitelist.add(new PhoneNumber(phone, label));
            labelView.setText("");
            phoneView.setText("");
            adapter.notifyDataSetChanged();
            parentingActivity.updateBackdrop(false);
        }
    }
}
