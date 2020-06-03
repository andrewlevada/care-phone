package com.andrewlevada.carephone.logic;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.logic.network.Network;

import java.util.ArrayList;
import java.util.List;

public class WhitelistAccesser {
    private static final String PREF_WHITELIST_LENGTH = "PREF_WHITELIST_LENGTH";
    private static final String PREF_WHITELIST_PHONE = "PREF_WHITELIST_PHONE";
    private static final String PREF_WHITELIST_LABEL = "PREF_WHITELIST_LABEL";

    private static WhitelistAccesser instance;

    private List<PhoneNumber> whitelist;
    private SharedPreferences preferences;
    private Context context;
    private RecyclerView.Adapter adapter;

    public void initialize(Context context) {
        this.context = context;
        whitelist = new ArrayList<>();
        loadFromLocal();
        syncWhitelist();
    }

    public void setAdapter(RecyclerView.Adapter newAdapter) {
        adapter = newAdapter;
    }

    private void loadFromLocal() {
        requirePreferences();

        int length = preferences.getInt(PREF_WHITELIST_LENGTH, 0);

        for (int i = 0; i < length; i++) {
            String phone = preferences.getString(PREF_WHITELIST_PHONE + i, "");
            String label = preferences.getString(PREF_WHITELIST_LABEL + i, "");
            whitelist.add(new PhoneNumber(phone, label));
        }
    }

    public boolean isInList(@NonNull String number) {
        number = number.replaceAll("\\s","").toLowerCase();

        for (PhoneNumber phoneNumber : whitelist) {
            String tempPhoneNumber = phoneNumber.phone.replaceAll("\\s","").toLowerCase();
            if (number.equals(tempPhoneNumber)) return true;

            while (number.length() != tempPhoneNumber.length()) {
                if (number.length() > tempPhoneNumber.length()) number = number.substring(1);
                else tempPhoneNumber = tempPhoneNumber.substring(1);
            }

            if (number.equals(tempPhoneNumber)) return true;
        }

        return false;
    }

    private void saveToLocal() {
        requirePreferences();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_WHITELIST_LENGTH, whitelist.size());

        for (int i = 0; i < whitelist.size(); i++) {
            editor.putString(PREF_WHITELIST_PHONE + i, whitelist.get(i).phone);
            editor.putString(PREF_WHITELIST_LABEL + i, whitelist.get(i).label);
        }

        editor.apply();
    }

    private void requirePreferences() {
        if (preferences == null)
            preferences = context.getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);
    }

    public PhoneNumber getWhitelistElement(int index) {
        return whitelist.get(index);
    }

    public void setWhitelistElement(int index, PhoneNumber element) {
        String prevPhone = whitelist.get(index).phone;
        Network.getInstance().editWhitelistRecord(prevPhone, element, null);
        whitelist.set(index, element);
        saveToLocal();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void addToWhitelist(PhoneNumber phoneNumber) {
        Network.getInstance().addToWhitelist(phoneNumber, null);
        whitelist.add(phoneNumber);
        saveToLocal();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public PhoneNumber removePhoneNumberAt(int index) {
        PhoneNumber result = whitelist.remove(index);
        saveToLocal();
        Network.getInstance().removeFromWhitelist(result.phone, null);
        return result;
    }

    public boolean removeFromWhitelist(@NonNull PhoneNumber o) {
        boolean result = whitelist.remove(o);
        saveToLocal();
        if (result) Network.getInstance().removeFromWhitelist(o.phone, null);
        return result;
    }

    public int getWhitelistSize() {
        return whitelist.size();
    }

    public void syncWhitelist() {
        Network.getInstance().syncWhitelist(new Network.NetworkCallbackOne<List<PhoneNumber>>() {
            @Override
            public void onSuccess(List<PhoneNumber> arg) {
                whitelist = arg;
                adapter.notifyDataSetChanged();
                saveToLocal();
            }

            @Override
            public void onFailure(Throwable throwable) {
                // TODO: Process failure
            }
        });
    }

    public static WhitelistAccesser getInstance() {
        if (instance == null) instance = new WhitelistAccesser();
        return instance;
    }
}
