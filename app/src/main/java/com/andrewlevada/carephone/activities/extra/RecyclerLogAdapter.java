package com.andrewlevada.carephone.activities.extra;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.LogFragment;
import com.andrewlevada.carephone.logic.LogRecord;

import java.util.List;

public class RecyclerLogAdapter extends RecyclerAdapter {
    private List<LogRecord> dataset;
    private OnEndReachedCallback callback;

    public RecyclerLogAdapter(RecyclerView recyclerView, List<LogRecord> dataset, OnEndReachedCallback callback) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_log_template;

        this.dataset = dataset;
        this.callback = callback;
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        LogRecord log = dataset.get(position);
        ((TextView) item.findViewById(R.id.recycler_phone)).setText(log.phoneNumber);
        ((TextView) item.findViewById(R.id.recycler_date)).setText(log.startTimestamp.toString());

        // Duration
        int duration = log.secondsDuration;
        String label;
        if (duration < 60) {
            label = "с";
        } else if (duration >= 60 && duration < 60 * 60) {
            duration /= 60;
            label = "м";
        } else {
            duration /= 60 * 60;
            label = "ч";
        }

        ((TextView) item.findViewById(R.id.recycler_duration)).setText(duration + " " + label);

        // Icon
        if (log.type == LogFragment.TYPE_INCOMING)
            ((ImageView) item.findViewById(R.id.recycler_type_icon)).setImageResource(R.drawable.ic_call_received);
        else if (log.type == LogFragment.TYPE_OUTGOING)
            ((ImageView) item.findViewById(R.id.recycler_type_icon)).setImageResource(R.drawable.ic_call_made);
        else ((ImageView) item.findViewById(R.id.recycler_type_icon)).setImageResource(R.drawable.ic_close);

        // Only for last element
        if (position == getItemCount() - 1) {
            item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);
            callback.reached();
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public interface OnEndReachedCallback {
        void reached();
    }
}
