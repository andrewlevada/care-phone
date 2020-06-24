package com.andrewlevada.carephone.activities.extra;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.WhitelistAccesser;

public class RecyclerWhitelistAdapter extends RecyclerAdapter {
    private Context context;
    private WhitelistAccesser whitelistAccesser;
    private boolean isExtended;

    public RecyclerWhitelistAdapter(RecyclerView recyclerView) {
        super(recyclerView);
        itemLayout = R.layout.recyclable_phone_number_template;

        context = recyclerView.getContext();
        whitelistAccesser = WhitelistAccesser.getInstance();
        isExtended = false;
    }

    @Override
    void fillItemWithData(ViewGroup item, int position) {
        PhoneNumber number = whitelistAccesser.getWhitelistElement(position);

        ((TextView) item.findViewById(R.id.recycler_number)).setText(number.getPhone());
        ((TextView) item.findViewById(R.id.recycler_label)).setText(number.getLabel());

        // Show and process additional image button if recycler view is extended
        if (isExtended) {
            item.findViewById(R.id.recycler_img).setVisibility(View.VISIBLE);
            item.findViewById(R.id.recycler_img).setOnClickListener(v -> inflateActionsMenu(v, position));
        } else item.findViewById(R.id.recycler_img).setVisibility(View.GONE);

        // Hide divider on last element
        if (position == getItemCount() - 1) item.findViewById(R.id.recycler_divider).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return whitelistAccesser.getWhitelistSize();
    }

    public boolean isExtended() {
        return isExtended;
    }

    public void setExtended(boolean extended) {
        isExtended = extended;
        notifyDataSetChanged();
    }

    private void inflateActionsMenu(View v, int position) {
        PopupMenu popupMenu;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            popupMenu = new PopupMenu(v.getContext(), v, Gravity.NO_GRAVITY,
                    R.attr.popupMenuStyle, R.style.Widget_Custom_PopupMenu);
        else popupMenu = new PopupMenu(v.getContext(), v, Gravity.NO_GRAVITY);

        popupMenu.getMenuInflater().inflate(R.menu.whitelist_editor, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClick(position));
    }

    private class OnMenuItemClick implements PopupMenu.OnMenuItemClickListener {
        private int index;

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.whitelist_editor_delete) {
                whitelistAccesser.removePhoneNumberAt(index);
                notifyDataSetChanged();
            } else if (item.getItemId() == R.id.whitelist_editor_copy_phone) {
                Toolbox.putInClipboard(null, context.getString(R.string.general_phone),
                        whitelistAccesser.getWhitelistElement(index).getPhone());
            } else if (item.getItemId() == R.id.whitelist_editor_copy_label) {
                Toolbox.putInClipboard(context, context.getString(R.string.general_name),
                        whitelistAccesser.getWhitelistElement(index).getLabel());
            }

            return false;
        }

        OnMenuItemClick(int index) {
            this.index = index;
        }
    }
}
