package com.andrewlevada.carephone.activities.extra;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;

import java.util.List;

public class RecyclerOnlyPhoneAdapter extends RecyclerAdapter {
    private List<String> dataset;

    public RecyclerOnlyPhoneAdapter(RecyclerView recyclerView, List<String> dataset) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_phone_template;
        this.dataset = dataset;
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        ((TextView) item.findViewById(R.id.recycler_phone)).setText(dataset.get(position));

        // Hide divider on last element
        if (position == getItemCount() - 1) item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
