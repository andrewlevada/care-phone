package com.andrewlevada.carephone.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.activities.extra.BackdropActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CaretakerListActivity extends BackdropActivity {
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.activity_caretaker_list;
        layoutBackdropId = R.layout.activity_caretaker_list_backdrop;
        super.onCreate(savedInstanceState);

        // Find views by ids
        fab = findViewById(R.id.caretaker_list_fab);

        // Process fab onclick
        fab.setOnClickListener(v -> updateBackdrop(true));
    }

    @Override
    public void updateBackdrop(boolean extend) {
        super.updateBackdrop(extend);

        if (extend) fab.hide();
        else fab.show();
    }

    @Override
    public void fillBackdrop(int layout, @Nullable SimpleInflater.OnViewInflated callback, @Nullable View.OnClickListener resultOnClick) { }
}