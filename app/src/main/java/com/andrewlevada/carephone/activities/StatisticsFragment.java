package com.andrewlevada.carephone.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.RecyclerAdapter;
import com.andrewlevada.carephone.activities.extra.RecyclerHoursAdapter;
import com.andrewlevada.carephone.logic.StatisticsPack;
import com.andrewlevada.carephone.logic.network.Network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {
    private static final String PREF_UPDATE_DATE = "PREF_UPDATE_DATE";
    private static final String PREF_PERIOD_HOURS = "PREF_PERIOD_HOURS";
    private static final String PREF_PHONES_LENGTH = "PREF_PHONES_LENGTH";
    private static final String PREF_PHONES_HOURS = "PREF_PHONES_HOURS";
    private static final String PREF_PHONES_LABELS = "PREF_PHONES_LABELS";

    private RecyclerAdapter periodsAdapter;
    private RecyclerAdapter phonesAdapter;

    private String[] periodsLabels;
    private List<Integer> periodsHours;
    private List<String> phonesLabels;
    private List<Integer> phonesHours;

    private HomeActivity parentingActivity;

    // Required empty public constructor
    public StatisticsFragment() { }

    public StatisticsFragment(HomeActivity parentingActivity) {
        this.parentingActivity = parentingActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Try to get parenting activity if not given
        if (parentingActivity == null && container.getContext() instanceof HomeActivity)
            parentingActivity = (HomeActivity) container.getContext();

        // Setup recycler views
        loadDataFromLocal();

        periodsAdapter = setupRecyclerView(layout.findViewById(R.id.periods_recycler),
                Arrays.asList(getResources().getStringArray(R.array.periods_labels)), periodsHours);
        phonesAdapter = setupRecyclerView(layout.findViewById(R.id.phones_recycler),
                phonesLabels, phonesHours);

        return layout;
    }

    private void loadDataFromLocal() {
        if (getContext() == null) return;
        SharedPreferences preferences = getContext().getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);

        periodsLabels = getResources().getStringArray(R.array.periods_labels);
        periodsHours = new ArrayList<>();
        for (int i = 0; i < periodsLabels.length; i++)
            periodsHours.add(preferences.getInt(PREF_PERIOD_HOURS + i, 0));

        phonesLabels = new ArrayList<>();
        phonesHours = new ArrayList<>();
        int phonesLength = preferences.getInt(PREF_PHONES_LENGTH, 0);
        for (int i = 0; i < phonesLength; i++) {
            phonesLabels.add(preferences.getString(PREF_PHONES_LABELS + i, ""));
            phonesHours.add(preferences.getInt(PREF_PHONES_HOURS + i, 0));
        }

        // Date nextUpdateDate = new Date(preferences.getLong(PREF_UPDATE_DATE, 0));
        // if (nextUpdateDate.before(new Date(System.currentTimeMillis()))) syncData();
        syncData();
    }

    private void syncData() {
        Network.router().syncStatistics(parentingActivity.isRemote, new Network.NetworkCallbackOne<StatisticsPack>() {
            @Override
            public void onSuccess(StatisticsPack statisticsPack) {
                processStatisticsPack(statisticsPack);
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                // TODO: Process failure
            }
        });
    }

    private void processStatisticsPack(StatisticsPack statisticsPack) {
        periodsHours = statisticsPack.getPeriodsHours();
        phonesLabels = statisticsPack.getPhonesLabels();
        phonesHours = statisticsPack.getPhonesHours();

        periodsAdapter.notifyDataSetChanged();
        phonesAdapter.notifyDataSetChanged();

        if (getContext() == null) return;
        SharedPreferences.Editor preferences = getContext().getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE).edit();

        for (int i = 0; i < periodsHours.size(); i++)
            preferences.putInt(PREF_PERIOD_HOURS + i, periodsHours.get(i));

        preferences.putInt(PREF_PHONES_LENGTH, phonesLabels.size());
        for (int i = 0; i < phonesLabels.size(); i++) {
            preferences.putString(PREF_PHONES_LABELS + i, phonesLabels.get(i));
            preferences.putInt(PREF_PHONES_HOURS + i, phonesHours.get(i));
        }

        preferences.putLong(PREF_UPDATE_DATE, System.currentTimeMillis() + Config.statisticsSyncPeriodHours * 60 * 60 * 1000);
        preferences.apply();
    }

    private RecyclerAdapter setupRecyclerView(RecyclerView recyclerView, List<String> labels, List<Integer> hours) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerAdapter adapter = new RecyclerHoursAdapter(recyclerView, labels, hours);
        recyclerView.setAdapter(adapter);

        return adapter;
    }
}
