package com.andrewlevada.carephone.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.andrewlevada.carephone.Toolbox;
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

        // Try to get parenting activity if not given
        if (parentingActivity == null && context instanceof HomeActivity)
            parentingActivity = (HomeActivity) context;

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
        Toolbox.getSyncThread(this, () -> {
            syncWhitelistState();
            WhitelistAccesser.getInstance().syncWhitelist();
        }).start();

        stateOnclick.setOnClickListener(v -> {
            Network.cared().setWhitelistState(!whitelistState, new Network.NetworkCallbackZero() {
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

        if (parentingActivity.isRemote) {
            layout.findViewById(R.id.whitelist_link_layout).setVisibility(View.GONE);
        } else {
            // Link user onclick processing
            layout.findViewById(R.id.whitelist_link_inner_layout).setOnClickListener(
                    v -> parentingActivity.fillBackdrop(R.layout.backdrop_content_whitelist_link,
                            view -> {
                                Network.cared().makeLinkRequest(new Network.NetworkCallbackOne<String>() {
                                    @Override
                                    public void onSuccess(String arg) {
                                        ((TextView) view.findViewById(R.id.backdrop_code)).setText(arg);
                                        parentingActivity.doCloseLinkOnBackdropCollapse = true;
                                    }

                                    @Override
                                    public void onFailure(@Nullable Throwable throwable) {
                                        // TODO: Process failure better
                                        ((TextView) view.findViewById(R.id.backdrop_code)).setText("000000");
                                    }
                                });
                                parentingActivity.updateBackdrop(true);
                            }, null));
        }

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

        // Hide linking button if remote
        if (parentingActivity.isRemote)
            layout.findViewById(R.id.whitelist_link_layout).setVisibility(View.GONE);

        // Fill backdrop after delay
        if (doExtend) new Handler().postDelayed(() ->
                parentingActivity.fillBackdrop(R.layout.backdrop_content_whitelist_add, null, new OnBackdropResultClick()), 650);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerWhitelistAdapter(recyclerView);
        recyclerView.setAdapter(adapter);
    }

    private void syncWhitelistState() {
        Network.router().getWhitelistState(parentingActivity.isRemote, new Network.NetworkCallbackOne<Boolean>() {
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
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator backgroundAnimation = ObjectAnimator.ofArgb(stateOnclick.getBackground(), "color",
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

            // Process
            if (phone.length() > 1 && phone.substring(0, 1).equals("8")) {
                phone = "+7" + phone.substring(1);
            }

            // Checks
            if (label.length() == 0) {
                labelView.setError(getString(R.string.whitelist_enter_name));
                return;
            } else if (label.length() > 20) {
                labelView.setError(getString(R.string.whitelist_long_name));
                return;
            }

            if (phone.length() == 0) {
                phoneView.setError(getString(R.string.general_enter_phone));
                return;
            } else if (phone.length() != 12 || !phone.contains("+")) {
                phoneView.setError(getString(R.string.general_wrong_phone));
                return;
            }

            WhitelistAccesser.getInstance().addToWhitelist(new PhoneNumber(phone, label));

            labelView.setText("");
            phoneView.setText("");
            parentingActivity.updateBackdrop(false);
        }
    }
}
