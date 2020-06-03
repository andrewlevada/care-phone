package com.andrewlevada.carephone.activities.extra;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.Toolbox;

import java.util.List;

public class RecyclerHoursAdapter extends RecyclerView.Adapter<RecyclerHoursAdapter.BasicViewHolder> {
    private List<String> labels;
    private List<Integer> hours;

    private Context context;

    public RecyclerHoursAdapter(RecyclerView recyclerView, List<String> labels, List<Integer> hours) {
        this.labels = labels;
        this.hours = hours;

        context = recyclerView.getContext();
    }

    @NonNull
    @Override
    public BasicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = SimpleInflater.inflate(parent, R.layout.recyclable_hours_template, false);
        return new BasicViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final BasicViewHolder holder, final int position) {
        if (context == null) return;

        ViewGroup item = (ViewGroup) holder.itemView;

        ((TextView) item.findViewById(R.id.text_label)).setText(labels.get(position));
        ((TextView) item.findViewById(R.id.text_hours)).setText(Toolbox.intToHoursString(hours.get(position)));
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    static class BasicViewHolder extends RecyclerView.ViewHolder {
        BasicViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
