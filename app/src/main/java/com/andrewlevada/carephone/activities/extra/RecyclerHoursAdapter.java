package com.andrewlevada.carephone.activities.extra;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;

import java.util.List;

public class RecyclerHoursAdapter extends RecyclerAdapter {
    private List<String> labels;
    private List<Integer> hours;

    public RecyclerHoursAdapter(RecyclerView recyclerView, List<String> labels, List<Integer> hours) {
        super(recyclerView);
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
        ((TextView) item.findViewById(R.id.text_hours)).setText(Toolbox.intToHoursString(hours.get(position)));
    }
}
