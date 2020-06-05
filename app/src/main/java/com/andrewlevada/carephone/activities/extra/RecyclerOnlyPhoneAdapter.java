package com.andrewlevada.carephone.activities.extra;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.logic.CaredUser;

import java.util.List;

public class RecyclerOnlyPhoneAdapter extends RecyclerAdapter {
    private List<CaredUser> dataset;

    public RecyclerOnlyPhoneAdapter(RecyclerView recyclerView, List<CaredUser> dataset) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_phone_template;
        this.dataset = dataset;
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        ((TextView) item.findViewById(R.id.recycler_phone)).setText(dataset.get(position).phone);

        // Hide divider on last element
        if (position == getItemCount() - 1) item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
