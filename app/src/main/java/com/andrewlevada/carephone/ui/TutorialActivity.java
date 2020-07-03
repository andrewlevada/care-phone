package com.andrewlevada.carephone.ui;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.andrewlevada.carephone.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class TutorialActivity extends AppCompatActivity {
    public static final String INTENT_USER_TYPE = "user_type";
    public static final String INTENT_NEXT_ACTIVITY = "next_activity";

    private String nextActivityName;
    private ViewPager2 pager;

    private String[] texts;
    private TypedArray drawables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        int userType = getIntent().getIntExtra(INTENT_USER_TYPE, AuthActivity.TYPE_CARED);
        nextActivityName = getIntent().getStringExtra(INTENT_NEXT_ACTIVITY);

        // Find views by ids
        pager = findViewById(R.id.pager);

        // Get data
        @ArrayRes int textsResource;
        @ArrayRes int drawablesResource;

        if (userType == AuthActivity.TYPE_CARED) {
            textsResource = R.array.tutorial_cared;
            drawablesResource = R.array.tutorial_cared_drawables;
        } else if (userType == AuthActivity.TYPE_CARETAKER) {
            textsResource = R.array.tutorial_caretaker;
            drawablesResource = R.array.tutorial_caretaker_drawables;
        } else return;

        texts = getResources().getStringArray(textsResource);
        drawables = getResources().obtainTypedArray(drawablesResource);

        // Setup pager adapter
        pager.setAdapter(new TutorialPageAdapter(this));
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == texts.length) finished();
            }
        });

        // Onclick processing
        findViewById(R.id.onclick).setOnClickListener(view -> {
            if (pager.getCurrentItem() == texts.length - 1) finished();
            else pager.setCurrentItem(pager.getCurrentItem() + 1, true);
        });
    }

    private void finished() {
        if (nextActivityName != null) {
            try {
                startActivity(new Intent(TutorialActivity.this, Class.forName(nextActivityName)));
            } catch (ClassNotFoundException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }

        drawables.recycle();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) super.onBackPressed();
        else pager.setCurrentItem(pager.getCurrentItem() - 1, true);
    }

    private class TutorialPageAdapter extends FragmentStateAdapter {
        public TutorialPageAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == texts.length) return new TutorialFragment();
            return new TutorialFragment(texts[position], drawables.getResourceId(position, 0));
        }

        @Override
        public int getItemCount() {
            return texts.length + 1;
        }
    }
}
