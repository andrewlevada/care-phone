package com.andrewlevada.carephone.ui.extra.recycleradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.logic.CaredUser;

import java.util.List;

public class RecyclerOnlyPhoneAdapter extends RecyclerAdapter {
    private List<CaredUser> dataset;
    private OnRecyclerItemClick onItemClick;

    public RecyclerOnlyPhoneAdapter(RecyclerView recyclerView, List<CaredUser> dataset, OnRecyclerItemClick onItemClick) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_phone_template;
        this.dataset = dataset;
        this.onItemClick = onItemClick;
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        ((TextView) item.findViewById(R.id.recycler_phone)).setText(dataset.get(position).getPhone());

        // Hide divider on last element
        if (position == getItemCount() - 1) item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);

        item.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(position); });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
