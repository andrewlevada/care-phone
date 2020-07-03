package com.andrewlevada.carephone.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.ui.AuthActivity;
import com.andrewlevada.carephone.ui.extra.CommonSettings;
import com.andrewlevada.carephone.ui.extra.recycleradapters.RecyclerAdapter;
import com.andrewlevada.carephone.ui.extra.recycleradapters.RecyclerClickMenuAdapter;

import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    private HomeActivity parentingActivity;
    private RecyclerClickMenuAdapter adapter;

    private RecyclerView recyclerView;

    // Required empty public constructor
    public SettingsFragment() { }

    public SettingsFragment(HomeActivity parentingActivity) {
        this.parentingActivity = parentingActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_settings, container, false);

        // Try to get parenting activity if not given
        if (parentingActivity == null && container.getContext() instanceof HomeActivity)
            parentingActivity = (HomeActivity) container.getContext();

        // Find views by ids
        recyclerView = layout.findViewById(R.id.recycler_view);

        setupRecyclerView();

        return layout;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerClickMenuAdapter(recyclerView,
                Arrays.asList(getResources().getStringArray(R.array.cared_settings)),
                new SettingsRecyclerOnclick());

        recyclerView.setAdapter(adapter);
    }

    private class SettingsRecyclerOnclick implements RecyclerAdapter.OnRecyclerItemClick {
        @Override
        public void onClick(int index) {
            if (index == SettingsItems.tutorial.ordinal()) {
                CommonSettings.showTutorial(parentingActivity, AuthActivity.TYPE_CARED);
            } else if (index == SettingsItems.changeUserType.ordinal())
                CommonSettings.switchActivityToHello(parentingActivity);
            else if (index == SettingsItems.logout.ordinal())
                CommonSettings.logout(parentingActivity);
            else if (index == SettingsItems.thanks.ordinal())
                CommonSettings.showThanksDialog(parentingActivity);
        }
    }

    private enum SettingsItems {
        tutorial,
        changeUserType,
        logout,
        thanks
    }
}
