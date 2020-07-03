package com.andrewlevada.carephone.ui.home;

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

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.WhitelistAccesser;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.extra.recycleradapters.RecyclerWhitelistAdapter;

import java.util.List;

public class WhitelistFragment extends Fragment {
    private WhitelistAccesser whitelistAccesser;
    
    private RecyclerView recyclerView;
    private ConstraintLayout layout;
    private View whitelistOnclick;
    private View stateOnclick;
    private View whitelistEmptyView;
    private TextView stateText;

    private ConstraintSet defaultConstraint;
    private ConstraintSet fullscreenConstraint;

    private HomeActivity parentingActivity;
    private RecyclerWhitelistAdapter adapter;

    private boolean isFullscreen;
    private boolean memoryWhitelistState;
    private boolean skipWhitelistStateSync;
    private boolean isEmpty;
    private boolean doHideEmpty;

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

        // Get views by ids
        Toolbar toolbar = layout.findViewById(R.id.whitelist_fullscreen_toolbar);
        recyclerView = layout.findViewById(R.id.whitelist_recycler);
        whitelistOnclick = layout.findViewById(R.id.whitelist_onclick);
        stateOnclick = layout.findViewById(R.id.whitelist_state_inner_layout);
        whitelistEmptyView = layout.findViewById(R.id.whitelist_empty_layout);
        stateText = layout.findViewById(R.id.whitelist_state_text);

        // Setup Whitelist Processing
        setupRecyclerView();
        setupWhitelistAccesser();

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
        if (!memoryWhitelistState) {
            stateText.setText(R.string.whitelist_state_turn_on);
            ((GradientDrawable) stateOnclick.getBackground()).setColor(context.getResources().getColor(R.color.colorOnSurface));
            stateText.setTextColor(context.getResources().getColor(R.color.colorSurface));
        }

        // Data sync setup
        Toolbox.getSyncThread(this, () -> {
            whitelistAccesser.syncWhitelist();
            if (skipWhitelistStateSync) skipWhitelistStateSync = false;
            else whitelistAccesser.syncWhitelistState();
        }).start();

        // State change button onclick listener
        stateOnclick.setOnClickListener(v -> {
            whitelistAccesser.setWhitelistState(!whitelistAccesser.getWhitelistState());
            skipWhitelistStateSync = true;
        });

        // Disable linking without internet and from remote
        if (!Toolbox.InternetConnectionChecker.getInstance().hasInternetSync()
                || parentingActivity.isRemote) {
            layout.findViewById(R.id.whitelist_link_layout).setVisibility(View.GONE);
        } else {
            // Link user onclick processing
            layout.findViewById(R.id.whitelist_link_inner_layout).setOnClickListener(
                    v -> parentingActivity.fillCloud(R.layout.cloud_content_whitelist_link,
                            view -> {
                                Network.cared().makeLinkRequest(new Network.NetworkCallbackOne<String>() {
                                    @Override
                                    public void onSuccess(String arg) {
                                        ((TextView) view.findViewById(R.id.cloud_code)).setText(arg);
                                        parentingActivity.doCloseLinkOnCloudCollapse = true;
                                    }

                                    @Override
                                    public void onFailure(@Nullable Throwable throwable) {
                                        // TODO: Process failure better
                                        ((TextView) view.findViewById(R.id.cloud_code))
                                                .setText(R.string.whitelist_link_blank_code);
                                    }
                                });
                                parentingActivity.updateCloud(true);
                            }, null));
        }

        return layout;
    }

    private void updateFullscreen(boolean doExtend) {
        ConstraintSet constraintSet;

        isFullscreen = doExtend;
        whitelistAccesser.syncWhitelist();
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
        transition.setInterpolator(new FastOutSlowInInterpolator());

        // Make transition
        TransitionManager.beginDelayedTransition(layout, transition);
        constraintSet.applyTo(layout);

        // Hide linking button if remote
        if (!Toolbox.InternetConnectionChecker.getInstance().hasInternetSync()
                || parentingActivity.isRemote)
            layout.findViewById(R.id.whitelist_link_layout).setVisibility(View.GONE);

        // Empty label process
        doHideEmpty = doExtend;
        if (doExtend) {
            recyclerView.setVisibility(View.VISIBLE);
            whitelistEmptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            whitelistEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        // Fill cloud after delay
        if (doExtend) new Handler().postDelayed(() ->
                parentingActivity.fillCloud(R.layout.cloud_content_whitelist_add, null, new OnCloudResultClick()), 650);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerWhitelistAdapter(recyclerView);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0) parentingActivity.hideFAB();
                else if (dy < 0) parentingActivity.requestFAB(new OnFABClick());
            }
        });
    }

    private void animateUpdateStateButton(boolean whitelistState) {
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

    private void setupWhitelistAccesser() {
        whitelistAccesser = WhitelistAccesser.getInstance();
        memoryWhitelistState = whitelistAccesser.getWhitelistState();
        whitelistAccesser.setAdapter(adapter);
        whitelistAccesser.setWhitelistChangedCallback(new OnGotWhitelist());
        whitelistAccesser.setWhitelistStateChangedCallback(new OnGotWhitelistState());
        whitelistAccesser.syncWhitelist();
        whitelistAccesser.syncWhitelistState();
    }

    private class OnGotWhitelistState implements Toolbox.CallbackOne<Boolean> {
        @Override
        public void invoke(Boolean arg) {
            if (arg) stateText.setText(R.string.whitelist_state_turn_off);
            else stateText.setText(R.string.whitelist_state_turn_on);
            if (arg != memoryWhitelistState) animateUpdateStateButton(!arg);
            memoryWhitelistState = arg;
        }
    }

    private class OnGotWhitelist implements Toolbox.CallbackOne<List<PhoneNumber>> {
        @Override
        public void invoke(List<PhoneNumber> whitelist) {
            isEmpty = whitelist.size() == 0;

            if (doHideEmpty) return;
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            whitelistEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private class OnFABClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            parentingActivity.updateCloud(true);
        }
    }

    private class OnCloudResultClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ViewGroup parent = (ViewGroup) v.getParent();
            TextView labelView = parent.findViewById(R.id.cloud_text_name);
            String label = labelView.getText().toString();
            TextView phoneView = parent.findViewById(R.id.cloud_text_phone);
            String phone = Toolbox.processPhone(phoneView.getText().toString());

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

            whitelistAccesser.addToWhitelist(new PhoneNumber(phone, label));

            labelView.setText("");
            phoneView.setText("");
            parentingActivity.updateCloud(false);
        }
    }
}
