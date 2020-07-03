package com.andrewlevada.carephone.ui.extra.recycleradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;

import java.util.List;

public class RecyclerClickMenuAdapter extends RecyclerAdapter {
    private List<String> dataset;
    private OnRecyclerItemClick onItemClick;

    public RecyclerClickMenuAdapter(@NonNull RecyclerView recyclerView,
                                    @NonNull List<String> dataset,
                                    @NonNull OnRecyclerItemClick onItemClick) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_oneline_template;
        this.dataset = dataset;
        this.onItemClick = onItemClick;
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        ((TextView) item.findViewById(R.id.recycler_text)).setText(dataset.get(position));

        // Hide divider on last element
        if (position == getItemCount() - 1) item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);

        item.setOnClickListener(v -> { if (onItemClick != null) onItemClick.onClick(position); });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
