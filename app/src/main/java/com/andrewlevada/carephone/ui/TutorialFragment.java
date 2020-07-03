package com.andrewlevada.carephone.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;

import com.andrewlevada.carephone.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialFragment extends Fragment {
    private String text;
    private int imageRes;

    // Required fragment_tutorial_empty public constructor
    public TutorialFragment() { }

    public TutorialFragment(String text, @DrawableRes int imageRes) {
        this.text = text;
        this.imageRes = imageRes;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(text != null ? R.layout.fragment_tutorial : R.layout.fragment_tutorial_empty,
                container, false);

        if (getContext() == null || text == null) return layout;

        // Find views by ids
        TextView textView = layout.findViewById(R.id.text);
        ImageView imageView = layout.findViewById(R.id.image);

        // Fill data
        textView.setText(text);
        imageView.setImageDrawable(getContext().getDrawable(imageRes));

        return layout;
    }
}