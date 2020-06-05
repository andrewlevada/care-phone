package com.andrewlevada.carephone.logic.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.StatisticsPack;

import java.util.List;

public class NetworkBothRouter extends Network {
    static NetworkBothRouter instance;

    // Whitelist

    public void syncWhitelist(boolean isRemote, @NonNull final NetworkCallbackOne<List<PhoneNumber>> callback) {
        if (isRemote) Network.caretaker().syncWhitelistR(callback);
        else Network.cared().syncWhitelist(callback);
    }

    public void addToWhitelist(boolean isRemote, @NonNull PhoneNumber phoneNumber, @Nullable final NetworkCallbackZero callback) {
        if (isRemote) Network.caretaker().addToWhitelistR(phoneNumber, callback);
        else Network.cared().addToWhitelist(phoneNumber, callback);
    }

    public void removeFromWhitelist(boolean isRemote, @NonNull String phone, @Nullable final NetworkCallbackZero callback) {
        if (isRemote) Network.caretaker().removeFromWhitelistR(phone, callback);
        else Network.cared().removeFromWhitelist(phone, callback);
    }

    public void editWhitelistRecord(boolean isRemote, @NonNull String prevPhone, @NonNull PhoneNumber phoneNumber, @Nullable final NetworkCallbackZero callback) {
        if (isRemote) Network.caretaker().editWhitelistRecordR(prevPhone, phoneNumber, callback);
        else Network.cared().editWhitelistRecord(prevPhone, phoneNumber, callback);
    }

    // Whitelist State

    public void getWhitelistState(boolean isRemote, @NonNull final NetworkCallbackOne<Boolean> callback) {
        if (isRemote) Network.caretaker().getWhitelistStateR(callback);
        else Network.cared().getWhitelistState(callback);
    }

    public void setWhitelistState(boolean isRemote, @NonNull Boolean state, @Nullable final NetworkCallbackZero callback) {
        if (isRemote) Network.caretaker().setWhitelistStateR(state, callback);
        else Network.cared().setWhitelistState(state, callback);
    }

    // Statistics

    public void syncStatistics(boolean isRemote, @NonNull final NetworkCallbackOne<StatisticsPack> callback) {
        if (isRemote) Network.caretaker().syncStatisticsR(callback);
        else Network.cared().syncStatistics(callback);
    }

    // Log

    public void getLog(boolean isRemote, int limit, int offset, @NonNull final NetworkCallbackOne<List<LogRecord>> callback) {
        if (isRemote) Network.caretaker().getLogR(limit, offset, callback);
        else Network.cared().getLog(limit, offset, callback);
    }
}
