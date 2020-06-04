package com.andrewlevada.carephone.activities.extra;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.SimpleInflater;

public abstract class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.BasicViewHolder> {
    @LayoutRes
    int itemLayout;

    private Context context;

    public RecyclerAdapter(RecyclerView recyclerView) {
        context = recyclerView.getContext();
    }

    @NonNull
    @Override
    public BasicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = SimpleInflater.inflate(parent, itemLayout, false);
        return new BasicViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final BasicViewHolder holder, final int position) {
        if (context == null) return;

        ViewGroup item = (ViewGroup) holder.itemView;
        fillItemWithData(item, position);
    }

    abstract void fillItemWithData(ViewGroup item, int position);

    static class BasicViewHolder extends RecyclerView.ViewHolder {
        BasicViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
