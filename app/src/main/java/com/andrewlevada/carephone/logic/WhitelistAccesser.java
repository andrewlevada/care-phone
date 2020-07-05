package com.andrewlevada.carephone.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.network.Network;
import com.andrewlevada.carephone.ui.extra.recycleradapters.RecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhitelistAccesser {
    private static final String PREF_WHITELIST_LENGTH = "PREF_WHITELIST_LENGTH";
    private static final String PREF_WHITELIST_PHONE = "PREF_WHITELIST_PHONE";
    private static final String PREF_WHITELIST_LABEL = "PREF_WHITELIST_LABEL";
    private static final String PREF_WHITELIST_STATE = "PREFS_WHITELIST_STATE";

    // TODO: Fix memory leak
    private static WhitelistAccesser instance;

    private List<PhoneNumber> whitelist;
    private boolean whitelistState;

    private FirebaseAnalytics analytics;
    private SharedPreferences preferences;
    private Context context;

    private RecyclerAdapter adapter;
    private Toolbox.CallbackOne<List<PhoneNumber>> whitelistChangedCallback;
    private Toolbox.CallbackOne<Boolean> whitelistStateChangedCallback;

    private boolean isRemote;

    public void initialize(Context context, boolean isRemote) {
        this.context = context;
        this.isRemote = isRemote;
        whitelist = new ArrayList<>();
        analytics = FirebaseAnalytics.getInstance(context);

        loadFromLocal();
        syncWhitelist();
        syncWhitelistState();
    }

    public void setAdapter(RecyclerAdapter newAdapter) {
        adapter = newAdapter;
    }

    public void setWhitelistStateChangedCallback(Toolbox.CallbackOne<Boolean> whitelistStateChangedCallback) {
        this.whitelistStateChangedCallback = whitelistStateChangedCallback;
        whitelistStateChangedCallback.invoke(whitelistState);
    }

    public void setWhitelistChangedCallback(Toolbox.CallbackOne<List<PhoneNumber>> whitelistChangedCallback) {
        this.whitelistChangedCallback = whitelistChangedCallback;
        whitelistChangedCallback.invoke(whitelist);
    }

    private void loadFromLocal() {
        requirePreferences();

        int length = preferences.getInt(PREF_WHITELIST_LENGTH, 0);

        for (int i = 0; i < length; i++) {
            String phone = preferences.getString(PREF_WHITELIST_PHONE + i, "");
            String label = preferences.getString(PREF_WHITELIST_LABEL + i, "");
            whitelist.add(new PhoneNumber(phone, label));
        }

        whitelistState = preferences.getBoolean(PREF_WHITELIST_STATE, true);
    }

    public boolean doDeclineCall(@NonNull final String phone) {
        syncWhitelist();
        syncWhitelistState();

        if (!whitelistState) return false;

        for (PhoneNumber phoneNumber : whitelist)
            if (phoneNumber.getPhone().equalsIgnoreCase(phone)) {
                return false;
            }

        return true;
    }

    private void saveToLocal() {
        requirePreferences();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_WHITELIST_LENGTH, whitelist.size());

        for (int i = 0; i < whitelist.size(); i++) {
            editor.putString(PREF_WHITELIST_PHONE + i, whitelist.get(i).getPhone());
            editor.putString(PREF_WHITELIST_LABEL + i, whitelist.get(i).getLabel());
        }

        editor.putBoolean(PREF_WHITELIST_STATE, whitelistState);

        editor.apply();

        if (whitelistChangedCallback != null) whitelistChangedCallback.invoke(whitelist);
        if (whitelistStateChangedCallback != null) whitelistStateChangedCallback.invoke(whitelistState);
    }

    private void requirePreferences() {
        if (preferences == null)
            preferences = context.getSharedPreferences(Config.appSharedPreferences, Context.MODE_PRIVATE);
    }

    public PhoneNumber getWhitelistElement(int index) {
        return whitelist.get(index);
    }

    public void setWhitelistElement(int index, PhoneNumber element) {
        String prevPhone = whitelist.get(index).getPhone();
        Network.router().editWhitelistRecord(isRemote, prevPhone, element, null);
        whitelist.set(index, element);
        saveToLocal();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void addToWhitelist(PhoneNumber phoneNumber) {
        Network.router().addToWhitelist(isRemote, phoneNumber, null);
        whitelist.add(phoneNumber);
        saveToLocal();

        analytics.logEvent(Config.Analytics.eventAddToWhitelist, new Bundle());

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public PhoneNumber removePhoneNumberAt(int index) {
        PhoneNumber result = whitelist.remove(index);
        saveToLocal();
        Network.router().removeFromWhitelist(isRemote, result.getPhone(), null);
        analytics.logEvent(Config.Analytics.eventRemoveFromWhitelist, null);
        return result;
    }

    public boolean removeFromWhitelist(@NonNull PhoneNumber o) {
        boolean result = whitelist.remove(o);
        if (!result) return false;

        saveToLocal();
        Network.router().removeFromWhitelist(isRemote, o.getPhone(), null);
        analytics.logEvent(Config.Analytics.eventRemoveFromWhitelist, null);
        return true;
    }

    public int getWhitelistSize() {
        return whitelist.size();
    }

    public void syncWhitelist() {
        Network.router().syncWhitelist(isRemote, new Network.NetworkCallbackOne<List<PhoneNumber>>() {
            @Override
            public void onSuccess(List<PhoneNumber> arg) {
                if (whitelist.equals(arg)) return;

                if (whitelist.size() != arg.size()) analytics.setUserProperty(
                        Config.Analytics.userPropertyWhitelistLength, String.valueOf(arg.size()));

                whitelist = arg;
                if (adapter != null) adapter.notifyDataSetChanged();
                saveToLocal();
            }

            @Override
            public void onFailure(Throwable throwable) {
                // TODO: Process failure
            }
        });
    }

    public void syncWhitelistState() {
        Network.router().getWhitelistState(isRemote, new Network.NetworkCallbackOne<Boolean>() {
            @Override
            public void onSuccess(Boolean arg) {
                if (whitelistState == arg) return;

                whitelistState = arg;
                analytics.setUserProperty(Config.Analytics.userPropertyWhitelistState, arg.toString());
                saveToLocal();
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                //TODO: Process failure
            }
        });
    }

    public boolean getWhitelistState() {
        return whitelistState;
    }

    public void setWhitelistState(boolean newState) {
        Network.router().setWhitelistState(isRemote, newState, null);
        whitelistState = newState;
        saveToLocal();
        if (whitelistStateChangedCallback != null) whitelistStateChangedCallback.invoke(newState);
    }

    public void clearData() {
        if (whitelist == null) return;

        whitelist.clear();
        whitelistState = true;
        saveToLocal();
    }

    public List<PhoneNumber> getWhitelistCopy() {
        return new ArrayList<>(whitelist);
    }

    public void loadSyncSms(Context context, String message) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        if (whitelist == null) initialize(context, false);

        String data = SyncSmsSender.StringXORer.decode(message.substring(SyncSmsSender.smsPrefix.length()),
                FirebaseAuth.getInstance().getCurrentUser().getUid());

        Pattern whitelistPattern = Pattern.compile("(.{11})([^_]+)_");

        List<PhoneNumber> newWhitelist = new ArrayList<>();
        boolean newWhitelistState = false;

        try {
            boolean isWhitelistEmpty = true;
            Matcher whitelistMatcher = whitelistPattern.matcher(data);

            while (whitelistMatcher.find()) {
                isWhitelistEmpty = false;

                if (Config.smsSyncEmptyWhitelist.contains(
                        Objects.requireNonNull(whitelistMatcher.group(0)))) break;

                newWhitelist.add(new PhoneNumber(
                        whitelistMatcher.group(1), whitelistMatcher.group(2)));
            }

            if (isWhitelistEmpty) return;

            newWhitelistState = data.substring(data.length() - 1).equals("1");
        } catch (Exception ignored) { }

        whitelist.clear();
        whitelist.addAll(newWhitelist);
        whitelistState = newWhitelistState;

        saveToLocal();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @UiThread
    public static WhitelistAccesser getInstance() {
        if (instance == null) instance = new WhitelistAccesser();
        return instance;
    }
}
