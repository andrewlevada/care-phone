package com.andrewlevada.carephone.activities.extra;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.logic.PhoneNumber;

import java.util.List;

public class RecyclerNumberAdapter extends RecyclerView.Adapter<RecyclerNumberAdapter.BasicViewHolder> {
    private List<PhoneNumber> dataset;
    private Context context;
    private boolean isExtended;

    public RecyclerNumberAdapter(RecyclerView recyclerView, final List<PhoneNumber> dataset,
                                 boolean isExtended) {
        this.dataset = dataset;
        this.isExtended = isExtended;
        context = recyclerView.getContext();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int position = viewHolder.getAdapterPosition();
                        dataset.remove(position);
                        notifyDataSetChanged();
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public BasicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = SimpleInflater.inflate(parent, R.layout.recyclable_number_template, false);
        return new BasicViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final BasicViewHolder holder, final int position) {
        if (context == null) return;

        ViewGroup item = (ViewGroup) holder.itemView;
        PhoneNumber number = dataset.get(position);

        ((TextView) item.findViewById(R.id.recycler_number)).setText(number.number);
        ((TextView) item.findViewById(R.id.recycler_label)).setText(number.label);

        // Show and process additional image button if recycler view is extended
        if (isExtended) {
            item.findViewById(R.id.recycler_img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Extend more options
                }
            });
        } else item.findViewById(R.id.recycler_img).setVisibility(View.GONE);

        // Hide divider on last element
        if (position == dataset.size() - 1) item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public static class BasicViewHolder extends RecyclerView.ViewHolder {

        public BasicViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
