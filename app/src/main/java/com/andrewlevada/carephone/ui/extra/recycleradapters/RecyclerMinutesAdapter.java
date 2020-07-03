package com.andrewlevada.carephone.ui.extra.recycleradapters;

import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;

import java.util.List;

public class RecyclerMinutesAdapter extends RecyclerAdapter {
    private Resources res;
    private List<String> labels;
    private List<Integer> hours;

    public RecyclerMinutesAdapter(RecyclerView recyclerView, List<String> labels, List<Integer> hours) {
        super(recyclerView);
        res = recyclerView.getResources();
        itemLayout = R.layout.recyclable_hours_template;

        this.labels = labels;
        this.hours = hours;
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        ((TextView) item.findViewById(R.id.text_label)).setText(labels.get(position));
        ((TextView) item.findViewById(R.id.text_hours)).setText(
                Toolbox.getShortStringFromMinutes(res, hours.get(position)));
    }
}
