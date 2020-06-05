package com.andrewlevada.carephone.logic.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.StatisticsPack;

import java.util.List;

public class NetworkCared extends Network {
    static NetworkCared instance;

    // Users

    public void addUserIfNew(@Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> addUserIfNew(callback))) return;

        getRetrofitRequests().tryToPutUser(userToken).enqueue(getDefaultVoidCallback(callback));
    }

    // Whitelist

    public void syncWhitelist(@NonNull final NetworkCallbackOne<List<PhoneNumber>> callback) {
        if (queueIfNotAuthedYet(() -> syncWhitelist(callback))) return;

        getRetrofitRequests().getWhitelist(userToken).enqueue(getDefaultOneCallback(callback));
    }

    public void addToWhitelist(@NonNull PhoneNumber phoneNumber, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> addToWhitelist(phoneNumber, callback))) return;

        getRetrofitRequests().putWhitelist(userToken, phoneNumber.phone, phoneNumber.label)
                .enqueue(getDefaultVoidCallback(callback));
    }

    public void removeFromWhitelist(@NonNull String phone, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> removeFromWhitelist(phone, callback))) return;

        getRetrofitRequests().deleteWhitelist(userToken, phone).enqueue(getDefaultVoidCallback(callback));
    }

    public void editWhitelistRecord(@NonNull String prevPhone, @NonNull PhoneNumber phoneNumber, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> editWhitelistRecord(prevPhone, phoneNumber, callback))) return;

        getRetrofitRequests().postWhitelist(userToken, prevPhone, phoneNumber.phone, phoneNumber.label)
                .enqueue(getDefaultVoidCallback(callback));
    }

    // Whitelist State

    public void getWhitelistState(@NonNull final NetworkCallbackOne<Boolean> callback) {
        if (queueIfNotAuthedYet(() -> getWhitelistState(callback))) return;

        getRetrofitRequests().getWhitelistState(userToken).enqueue(getDefaultOneCallback(callback));
    }

    public void setWhitelistState(@NonNull Boolean state, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> setWhitelistState(state, callback))) return;

        getRetrofitRequests().postWhitelistState(userToken, state).enqueue(getDefaultVoidCallback(callback));
    }

    // Statistics

    public void syncStatistics(@NonNull final NetworkCallbackOne<StatisticsPack> callback) {
        if (queueIfNotAuthedYet(() -> syncStatistics(callback))) return;

        getRetrofitRequests().getStatisticsPack(userToken).enqueue(getDefaultOneCallback(callback));
    }

    // Log

    public void getLog(int limit, int offset, @NonNull final NetworkCallbackOne<List<LogRecord>> callback) {
        if (queueIfNotAuthedYet(() -> getLog(limit, offset, callback))) return;

        getRetrofitRequests().getLog(userToken, limit, offset).enqueue(getDefaultOneCallback(callback));
    }

    public void addToLog(@NonNull LogRecord logRecord, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> addToLog(logRecord, callback))) return;

        getRetrofitRequests().putLog(userToken, logRecord.phoneNumber, logRecord.startTimestamp,
                logRecord.secondsDuration, logRecord.type).enqueue(getDefaultVoidCallback(callback));
    }

    // Link

    public void makeLinkRequest(@Nullable final NetworkCallbackOne<String> callback) {
        if (queueIfNotAuthedYet(() -> makeLinkRequest(callback))) return;

        getRetrofitRequests().putLink(userToken).enqueue(getDefaultOneCallback(callback));
    }

    public void removeLinkRequest(@Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> removeLinkRequest(callback))) return;

        getRetrofitRequests().deleteLink(userToken).enqueue(getDefaultVoidCallback(callback));
    }
}
