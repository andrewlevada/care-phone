package com.andrewlevada.carephone.ui.extra.recycleradapters;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.ui.home.LogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerLogAdapter extends RecyclerAdapter {
    private static final String dateFormatString = "HH:mm dd LLLL";
    private Resources res;
    private List<LogRecord> dataset;
    private OnEndReachedCallback callback;
    private SimpleDateFormat dateFormat;

    public RecyclerLogAdapter(RecyclerView recyclerView, List<LogRecord> dataset, OnEndReachedCallback callback) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_log_template;
        res = recyclerView.getResources();

        this.dataset = dataset;
        this.callback = callback;
        dateFormat = new SimpleDateFormat(dateFormatString, Locale.getDefault());
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        LogRecord log = dataset.get(position);

        String phoneNumber = log.getPhoneNumber().length() == 0
                ? res.getString(R.string.log_unknown) : log.getPhoneNumber();
        ((TextView) item.findViewById(R.id.recycler_phone)).setText(phoneNumber);

        String startTimeText = dateFormat.format(new Date(log.getStartTimestamp()));
        ((TextView) item.findViewById(R.id.recycler_date)).setText(startTimeText);

        // Duration
        String durationText = log.getSecondsDuration() == 0 ? res.getString(R.string.log_blocked)
                : Toolbox.getShortStringFromSeconds(res, log.getSecondsDuration());
        ((TextView) item.findViewById(R.id.recycler_duration)).setText(durationText);

        // Icon
        ImageView iconImage = item.findViewById(R.id.recycler_type_icon);

        if (log.getType() == LogFragment.TYPE_INCOMING)
            iconImage.setImageResource(R.drawable.ic_call_received);
        else if (log.getType() == LogFragment.TYPE_OUTGOING)
            iconImage.setImageResource(R.drawable.ic_call_made);
        else if (log.getType() == LogFragment.TYPE_BLOCKED)
            iconImage.setImageResource(R.drawable.ic_call_blocked);
        else
            iconImage.setImageResource(R.drawable.ic_close);

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
