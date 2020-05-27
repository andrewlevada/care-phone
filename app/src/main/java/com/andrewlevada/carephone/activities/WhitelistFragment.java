package com.andrewlevada.carephone.activities;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.activities.extra.RecyclerNumberAdapter;
import com.andrewlevada.carephone.logic.WhitelistAccesser;

import static android.view.View.GONE;

public class WhitelistFragment extends Fragment {
    private RecyclerView recyclerView;
    private ConstraintLayout layout;
    private View onclick;

    private ConstraintSet defaultConstraint;
    private ConstraintSet fullscreenConstraint;

    private HomeActivity parentingActivity;

    private boolean isFullscreen;

    public WhitelistFragment() {
        // Required empty public constructor
    }

    public WhitelistFragment(HomeActivity parentingActivity) {
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

        // Load needed layout
        if (doExtend) constraintSet = fullscreenConstraint;
        else constraintSet = defaultConstraint;

        // Setup transition
        Transition transition = new AutoTransition();
        transition.setDuration(600);
        if (doExtend) transition.setInterpolator(new FastOutSlowInInterpolator());
        else transition.setInterpolator(new FastOutLinearInInterpolator());

        // Make transition
        TransitionManager.beginDelayedTransition(layout, transition);
        constraintSet.applyTo(layout);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerNumberAdapter adapter = new RecyclerNumberAdapter(recyclerView, WhitelistAccesser.whitelist, false);
        recyclerView.setAdapter(adapter);
    }
}
