package com.andrewlevada.carephone.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.R;
import com.andrewlevada.carephone.activities.extra.RecyclerHoursAdapter;

import java.util.ArrayList;
import java.util.Date;
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

    private HomeActivity parentingActivity;

    private RecyclerView.Adapter periodsAdapter;
    private RecyclerView.Adapter phonesAdapter;

    private List<String> periodsLabels;
    private List<Integer> periodsHours;
    private List<String> phonesLabels;
    private List<Integer> phonesHours;

    // Required empty public constructor
    public StatisticsFragment() { }

    StatisticsFragment(HomeActivity parentingActivity) {
        this.parentingActivity = parentingActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Setup recycler views
        loadDataFromLocal();

        periodsAdapter = setupRecyclerView(layout.findViewById(R.id.statistics_periods_recycler),
                periodsLabels, periodsHours);
        phonesAdapter = setupRecyclerView(layout.findViewById(R.id.statistics_phones_recycler),
                phonesLabels, phonesHours);

        return layout;
    }

    private void loadDataFromLocal() {
        if (getContext() == null) return;
        SharedPreferences preferences = getContext().getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);

        periodsLabels = Config.periodsLabels;
        periodsHours = new ArrayList<>();
        for (int i = 0; i < Config.periodsLabels.size(); i++)
            periodsHours.add(preferences.getInt(PREF_PERIOD_HOURS + i, 0));

        phonesLabels = new ArrayList<>();
        phonesHours = new ArrayList<>();
        int phonesLength = preferences.getInt(PREF_PHONES_LENGTH, 0);
        for (int i = 0; i < phonesLength; i++) {
            phonesLabels.add(preferences.getString(PREF_PHONES_LABELS + i, ""));
            phonesHours.add(preferences.getInt(PREF_PHONES_HOURS + i, 0));
        }

        Date nextUpdateDate = new Date(preferences.getLong(PREF_UPDATE_DATE, 0));
        if (nextUpdateDate.before(new Date(System.currentTimeMillis()))) syncData();
    }

    private void syncData() {
        // TODO: Sync data
    }

    private RecyclerView.Adapter setupRecyclerView(RecyclerView recyclerView, List<String> labels, List<Integer> hours) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new RecyclerHoursAdapter(recyclerView, labels, hours);
        recyclerView.setAdapter(adapter);

        return adapter;
    }
}
