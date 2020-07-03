package com.andrewlevada.carephone.ui.extra.recycleradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.ui.home.LogFragment;
import com.andrewlevada.carephone.logic.LogRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerLogAdapter extends RecyclerAdapter {
    private static final String dateFormatString = "HH:mm dd LLLL";
    private List<LogRecord> dataset;
    private OnEndReachedCallback callback;
    private SimpleDateFormat dateFormat;

    public RecyclerLogAdapter(RecyclerView recyclerView, List<LogRecord> dataset, OnEndReachedCallback callback) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_log_template;

        this.dataset = dataset;
        this.callback = callback;
        dateFormat = new SimpleDateFormat(dateFormatString, Locale.getDefault());
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        LogRecord log = dataset.get(position);
        ((TextView) item.findViewById(R.id.recycler_phone)).setText(log.getPhoneNumber());
        ((TextView) item.findViewById(R.id.recycler_date)).setText(dateFormat.format(new Date(log.getStartTimestamp())));

        // Duration
        int duration = log.getSecondsDuration();
        ((TextView) item.findViewById(R.id.recycler_duration)).setText(
                Toolbox.getShortStringFromTime(duration));

        // Icon
        if (log.getType() == LogFragment.TYPE_INCOMING)
            ((ImageView) item.findViewById(R.id.recycler_type_icon)).setImageResource(R.drawable.ic_call_received);
        else if (log.getType() == LogFragment.TYPE_OUTGOING)
            ((ImageView) item.findViewById(R.id.recycler_type_icon)).setImageResource(R.drawable.ic_call_made);
        else ((ImageView) item.findViewById(R.id.recycler_type_icon)).setImageResource(R.drawable.ic_close);

        // Only for last element
        if (position == 0) {
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
