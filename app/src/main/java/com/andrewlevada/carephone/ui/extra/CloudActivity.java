package com.andrewlevada.carephone.ui.extra;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.Toolbox;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * This class is an extension of normal Activity.
 * It implements interactions with Cloud ui element
 * (not described in Material Design docs). Cloud is
 * a window which covers the screen partially or fully
 * and most of the time contains editing actions.
 */
public abstract class CloudActivity extends AppCompatActivity {
    private ConstraintLayout layout;
    private ViewGroup cloud;

    private ConstraintSet defaultConstraint;
    private ConstraintSet cloudConstraint;

    @LayoutRes
    protected int layoutId;
    @LayoutRes
    protected int layoutCloudId;

    private Window window;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        // Find views by ids
        layout = findViewById(R.id.layout);
        cloud = findViewById(R.id.cloud);

        // Setup window for change of status bar color
        window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Setup cloud
        Point display = new Point();
        getWindowManager().getDefaultDisplay().getSize(display);
        cloud.getLayoutParams().height = display.y;

        // Setup ConstraintSets for cloud animations
        defaultConstraint = new ConstraintSet();
        defaultConstraint.clone(layout);
        cloudConstraint = new ConstraintSet();
        cloudConstraint.load(getApplicationContext(), layoutCloudId);

        // Setup cloud toolbar
        MaterialToolbar cloudToolbar = findViewById(R.id.cloud_toolbar);
        cloudToolbar.setNavigationOnClickListener(v -> updateCloud(false));
    }

    /**
     * If cloud is used in one activity several times,
     * you should leave in fragment_tutorial_empty in resource file
     * and use this method to fill it with some layout.
     * Do not fill it when animations are ongoing.
     * @param layout Layout resource id with which cloud is going to be filled
     * @param callback This callback is going to be called after cloud is filled as it fills async.
     * @param resultOnClick This callback is called when user presses the result button in cloud.
     */
    @UiThread
    public void fillCloud(@LayoutRes int layout, @Nullable final SimpleInflater.OnViewInflated callback, @Nullable final View.OnClickListener resultOnClick) {
        ViewGroup cloud = findViewById(R.id.cloud);
        cloud.removeAllViews();
        SimpleInflater.inflateSmooth(view -> {
            if (callback != null) callback.inflated(view);

            View resultButton = view.findViewById(R.id.cloud_result_button);
            if (resultButton != null && resultOnClick != null)
                resultButton.setOnClickListener(resultOnClick);
        }, findViewById(R.id.cloud), layout);
    }

    /**
     * Retracts or extends cloud.
     * @param extend This boolean indicates which state will cloud should have after running this method.
     */
    public void updateCloud(final boolean extend) {
        hideKeyboard();

        ConstraintSet constraintSet;

        // Load needed layout
        if (extend) constraintSet = cloudConstraint;
        else constraintSet = defaultConstraint;

        // Setup transition
        Transition transition = new AutoTransition();
        transition.setDuration(600);
        if (extend) transition.setInterpolator(new FastOutSlowInInterpolator());
        else transition.setInterpolator(new FastOutLinearInInterpolator());

        // Change status bar color
        if (extend)
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorOnSurfaceDark));
        else transition.addListener(new EndTransitionListener(arg ->
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))));

        TransitionManager.beginDelayedTransition(layout, transition);
        constraintSet.applyTo(layout);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputManager == null) return;
        inputManager.hideSoftInputFromWindow(cloud.getWindowToken(), 0);
    }

    private static class EndTransitionListener implements Transition.TransitionListener {
        private Toolbox.CallbackOne<Transition> callback;

        @Override
        public void onTransitionStart(@NonNull Transition transition) {
        }

        @Override
        public void onTransitionEnd(@NonNull Transition transition) {
            callback.invoke(transition);
        }

        @Override
        public void onTransitionCancel(@NonNull Transition transition) {
        }

        @Override
        public void onTransitionPause(@NonNull Transition transition) {
        }

        @Override
        public void onTransitionResume(@NonNull Transition transition) {
        }

        public EndTransitionListener(Toolbox.CallbackOne<Transition> callback) {
            this.callback = callback;
        }
    }
}
