package com.andrewlevada.carephone.activities.extra;

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
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BackdropActivity extends AppCompatActivity {
    private ConstraintLayout layout;
    private ViewGroup backdrop;

    private ConstraintSet defaultConstraint;
    private ConstraintSet backdropConstraint;

    @LayoutRes
    protected int layoutId;
    @LayoutRes
    protected int layoutBackdropId;

    private Window window;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        // Find views by ids
        layout = findViewById(R.id.layout);
        backdrop = findViewById(R.id.backdrop);

        // Setup window for change of status bar color
        window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Setup backdrop
        Point display = new Point();
        getWindowManager().getDefaultDisplay().getSize(display);
        backdrop.getLayoutParams().height = display.y;

        // Setup ConstraintSets for fullscreen animations
        defaultConstraint = new ConstraintSet();
        defaultConstraint.clone(layout);
        backdropConstraint = new ConstraintSet();
        backdropConstraint.load(getApplicationContext(), layoutBackdropId);

        // Setup backdrop toolbar
        MaterialToolbar backdropToolbar = findViewById(R.id.backdrop_toolbar);
        backdropToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBackdrop(false);
            }
        });
    }

    public void fillBackdrop(@LayoutRes int layout, @Nullable final SimpleInflater.OnViewInflated callback, @Nullable final View.OnClickListener resultOnClick) {
        ViewGroup backdrop = findViewById(R.id.backdrop);
        backdrop.removeAllViews();
        SimpleInflater.inflateSmooth(new SimpleInflater.OnViewInflated() {
            @Override
            public void inflated(View view) {
                if (callback != null) callback.inflated(view);

                View resultButton = view.findViewById(R.id.backdrop_result_button);
                if (resultButton != null && resultOnClick != null)
                    resultButton.setOnClickListener(resultOnClick);
            }
        }, (ViewGroup) findViewById(R.id.backdrop), layout);
    }

    public void updateBackdrop(final boolean extend) {
        hideKeyboard();

        ConstraintSet constraintSet;

        // Load needed layout
        if (extend) constraintSet = backdropConstraint;
        else constraintSet = defaultConstraint;

        // Change status bar color
        if (extend)
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorOnSurfaceDark));

        // Setup transition
        Transition transition = new AutoTransition();
        transition.setDuration(600);
        if (extend) transition.setInterpolator(new FastOutSlowInInterpolator());
        else transition.setInterpolator(new FastOutLinearInInterpolator());

        BackdropActivity itself = this;
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                if (!extend)
                    window.setStatusBarColor(ContextCompat.getColor(itself, R.color.colorPrimaryDark));
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
        });

        TransitionManager.beginDelayedTransition(layout, transition);
        constraintSet.applyTo(layout);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputManager == null) return;
        inputManager.hideSoftInputFromWindow(backdrop.getWindowToken(), 0);
    }
}
