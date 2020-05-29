package com.andrewlevada.carephone.activities.extra;

import android.content.Context;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.SimpleInflater;
import com.andrewlevada.carephone.logic.PhoneNumber;

import java.util.List;

public class RecyclerNumberAdapter extends RecyclerView.Adapter<RecyclerNumberAdapter.BasicViewHolder> {
    private List<PhoneNumber> dataset;
    private Context context;
    private boolean isExtended;

    public RecyclerNumberAdapter(RecyclerView recyclerView, final List<PhoneNumber> dataset) {
        this.dataset = dataset;
        context = recyclerView.getContext();
        isExtended = false;
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

        ((TextView) item.findViewById(R.id.recycler_number)).setText(number.phone);
        ((TextView) item.findViewById(R.id.recycler_label)).setText(number.label);

        // Show and process additional image button if recycler view is extended
        if (isExtended) {
            item.findViewById(R.id.recycler_img).setVisibility(View.VISIBLE);
            item.findViewById(R.id.recycler_img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.NO_GRAVITY, R.attr.popupMenuStyle, R.style.Widget_Custom_PopupMenu);
                    popupMenu.getMenuInflater().inflate(R.menu.whitelist_editor, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new OnMenuItemClick(position));
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

    public boolean isExtended() {
        return isExtended;
    }

    public void setExtended(boolean extended) {
        isExtended = extended;
        notifyDataSetChanged();
    }

    static class BasicViewHolder extends RecyclerView.ViewHolder {

        BasicViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class OnMenuItemClick implements PopupMenu.OnMenuItemClickListener {
        private int index;

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.whitelist_editor_edit) {
                // TODO: Implement editing on backdrop
            } else if (item.getItemId() == R.id.whitelist_editor_delete) {
                // TODO: Add warning for deleting
                dataset.remove(index);
                notifyDataSetChanged();
            }

            return false;
        }

        OnMenuItemClick(int index) {
            this.index = index;
        }
    }
}
