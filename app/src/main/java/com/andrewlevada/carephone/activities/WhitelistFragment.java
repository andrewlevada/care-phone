package com.andrewlevada.carephone.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.RecyclerWhitelistAdapter;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.network.Network;

public class WhitelistFragment extends Fragment {
    private static final String PREFS_STATE = "PREFS_WHITELIST_STATE";

    private RecyclerView recyclerView;
    private ConstraintLayout layout;
    private View whitelistOnclick;
    private View stateOnclick;
    private TextView stateText;

    private ConstraintSet defaultConstraint;
    private ConstraintSet fullscreenConstraint;

    private HomeActivity parentingActivity;
    private RecyclerWhitelistAdapter adapter;

    private boolean isFullscreen;
    private boolean whitelistState;

    private Context context;

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
        context = container.getContext();

        // Get views by id
        Toolbar toolbar = layout.findViewById(R.id.home_whitelist_fullscreen_toolbar);
        whitelistOnclick = layout.findViewById(R.id.home_whitelist_onclick);
        stateOnclick = layout.findViewById(R.id.whitelist_state_inner_layout);
        stateText = layout.findViewById(R.id.whitelist_state_text);

        // Setup recycler view
        recyclerView = layout.findViewById(R.id.home_whitelist_recycler);
        setupRecyclerView();

        // Setup Whitelist Accesser
        WhitelistAccesser.getInstance().setAdapter(adapter);
        WhitelistAccesser.getInstance().syncWhitelist();

        // Setup ConstraintSets for fullscreen animations
        defaultConstraint = new ConstraintSet();
        defaultConstraint.clone(layout);
        fullscreenConstraint = new ConstraintSet();
        fullscreenConstraint.load(context, R.layout.fragment_whitelist_fullscreen);

        // Try to get parenting activity if not given
        if (parentingActivity == null && context instanceof HomeActivity)
            parentingActivity = (HomeActivity) context;

        // Fill backdrop
        if (parentingActivity != null)
            parentingActivity.fillBackdrop(R.layout.backdrop_content_whitelist_add, null, new OnBackdropResultClick());

        // Whitelist onclick processing
        whitelistOnclick.setOnClickListener(v -> {
            if (isFullscreen) return;
            updateFullscreen(true);
        });

        // Fullscreen toolbar onclick processing
        toolbar.setNavigationOnClickListener(v -> {
            if (!isFullscreen) return;
            updateFullscreen(false);
        });

        // Whitelist State processing
        whitelistState = context.getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE)
                .getBoolean(PREFS_STATE, true);
        if (!whitelistState) {
            stateText.setText(R.string.whitelist_state_turn_on);
            ((GradientDrawable) stateOnclick.getBackground()).setColor(context.getResources().getColor(R.color.colorOnSurface));
            stateText.setTextColor(context.getResources().getColor(R.color.colorSurface));
        }

        syncWhitelistState();

        stateOnclick.setOnClickListener(v -> {
            Network.getInstance().setWhitelistState(!whitelistState, new Network.NetworkCallbackZero() {
                @Override
                public void onSuccess() {
                    syncWhitelistState();
                }

                @Override
                public void onFailure(@Nullable Throwable throwable) {
                    // TODO: Process failure
                }
            });
        });

        return layout;
    }

    private void updateFullscreen(boolean doExtend) {
        ConstraintSet constraintSet;

        isFullscreen = doExtend;
        WhitelistAccesser.getInstance().syncWhitelist();
        whitelistOnclick.setVisibility(doExtend ? View.GONE : View.VISIBLE);

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

        adapter = new RecyclerWhitelistAdapter(recyclerView);
        recyclerView.setAdapter(adapter);
    }

    private void syncWhitelistState() {
        Network.getInstance().getWhitelistState(new Network.NetworkCallbackOne<Boolean>() {
            @Override
            public void onSuccess(Boolean arg) {
                if (arg) stateText.setText(R.string.whitelist_state_turn_off);
                else stateText.setText(R.string.whitelist_state_turn_on);
                if (arg != whitelistState) animateUpdateStateButton();
                whitelistState = arg;

                context.getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE).edit().
                        putBoolean(PREFS_STATE, whitelistState).apply();
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                //TODO: Process failure
            }
        });
    }

    private void animateUpdateStateButton() {
        ObjectAnimator backgroundAnimation = ObjectAnimator.ofArgb(((GradientDrawable) stateOnclick.getBackground()), "color",
                ContextCompat.getColor(context, whitelistState ? R.color.colorSurface : R.color.colorOnSurface),
                ContextCompat.getColor(context, !whitelistState ? R.color.colorSurface : R.color.colorOnSurface));
        backgroundAnimation.setDuration(600);

        ObjectAnimator textAnimation = ObjectAnimator.ofArgb(stateText, "textColor",
                ContextCompat.getColor(context, whitelistState ? R.color.colorOnSurface : R.color.colorSurface),
                ContextCompat.getColor(context, !whitelistState ? R.color.colorOnSurface : R.color.colorSurface));
        textAnimation.setDuration(600);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(backgroundAnimation).with(textAnimation);
        animatorSet.start();
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

            WhitelistAccesser.getInstance().addToWhitelist(new PhoneNumber(phone, label));

            labelView.setText("");
            phoneView.setText("");
            parentingActivity.updateBackdrop(false);
        }
    }
}
