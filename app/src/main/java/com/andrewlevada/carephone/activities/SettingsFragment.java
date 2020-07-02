package com.andrewlevada.carephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.activities.extra.RecyclerAdapter;
import com.andrewlevada.carephone.activities.extra.RecyclerClickMenuAdapter;
import com.andrewlevada.carephone.logic.blockers.Blocker_L_to_N_MR1;
import com.google.firebase.auth.FirebaseAuth;

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
                Arrays.asList(getResources().getStringArray(R.array.settings_options)),
                new SettingsRecyclerOnclick());

        recyclerView.setAdapter(adapter);
    }

    private void addShortcut() {
        Intent shortcutIntent = new Intent(parentingActivity.getApplicationContext(), Blocker_L_to_N_MR1.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Запуск ЗаботоФОН");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(parentingActivity.getApplicationContext(), R.mipmap.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate" , false);

        parentingActivity.getApplicationContext().sendBroadcast(addIntent);

    }

    private class SettingsRecyclerOnclick implements RecyclerAdapter.OnRecyclerItemClick {
        @Override
        public void onClick(int index) {
            if (index == SettingsItems.changeUserType.ordinal()) switchActivityToHello();
            else if (index == SettingsItems.logout.ordinal()) logout();
            else if (index == SettingsItems.aboutApp.ordinal()) Toolbox.showSimpleDialog(parentingActivity,
                    R.string.settings_about_dialog_title, R.string.settings_about_dialog_message,
                    R.string.general_great);
        }
    }

    private void logout() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        FirebaseAuth.getInstance().signOut();
        switchActivityToHello();
    }

    private void switchActivityToHello() {
        Intent intent = new Intent(parentingActivity, HelloActivity.class);
        intent.putExtra(HelloActivity.INTENT_EXTRA_STAY, true);
        startActivity(intent);
        parentingActivity.finish();
    }

    private enum SettingsItems {
        changeUserType,
        logout,
        aboutApp
    }
}
