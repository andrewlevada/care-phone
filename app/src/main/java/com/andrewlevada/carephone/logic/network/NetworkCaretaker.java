package com.andrewlevada.carephone.logic.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andrewlevada.carephone.logic.CaredUser;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.StatisticsPack;

import java.util.List;

public class NetworkCaretaker extends Network {
    static NetworkCaretaker instance;

    public String rUid;

    // Whitelist

    public void syncWhitelistR(@NonNull final NetworkCallbackOne<List<PhoneNumber>> callback) {
        if (queueIfNotAuthedYet(() -> syncWhitelistR(callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().getWhitelistR(userToken, rUid).enqueue(getDefaultOneCallback(callback));
    }

    public void addToWhitelistR(@NonNull PhoneNumber phoneNumber, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> addToWhitelistR(phoneNumber, callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().putWhitelistR(userToken, rUid, phoneNumber.phone, phoneNumber.label)
                .enqueue(getDefaultVoidCallback(callback));
    }

    public void removeFromWhitelistR(@NonNull String phone, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> removeFromWhitelistR(phone, callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().deleteWhitelistR(userToken, rUid, phone).enqueue(getDefaultVoidCallback(callback));
    }

    public void editWhitelistRecordR(@NonNull String prevPhone, @NonNull PhoneNumber phoneNumber, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> editWhitelistRecordR(prevPhone, phoneNumber, callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().postWhitelistR(userToken, rUid, prevPhone, phoneNumber.phone, phoneNumber.label)
                .enqueue(getDefaultVoidCallback(callback));
    }

    // Whitelist State

    public void getWhitelistStateR(@NonNull final NetworkCallbackOne<Boolean> callback) {
        if (queueIfNotAuthedYet(() -> getWhitelistStateR(callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().getWhitelistStateR(userToken, rUid).enqueue(getDefaultOneCallback(callback));
    }

    public void setWhitelistStateR(@NonNull Boolean state, @Nullable final NetworkCallbackZero callback) {
        if (queueIfNotAuthedYet(() -> setWhitelistStateR(state, callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().postWhitelistStateR(userToken, rUid, state).enqueue(getDefaultVoidCallback(callback));
    }

    // Statistics

    public void syncStatisticsR(@NonNull final NetworkCallbackOne<StatisticsPack> callback) {
        if (queueIfNotAuthedYet(() -> syncStatisticsR(callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().getStatisticsPackR(userToken, rUid).enqueue(getDefaultOneCallback(callback));
    }

    // Log

    public void getLogR(int limit, int offset, @NonNull final NetworkCallbackOne<List<LogRecord>> callback) {
        if (queueIfNotAuthedYet(() -> getLogR(limit, offset, callback))) return;
        if (processRequestIfRUidNotSet(callback)) return;

        getRetrofitRequests().getLogR(userToken, rUid, limit, offset).enqueue(getDefaultOneCallback(callback));
    }

    // Cared List

    public void getCaredList(@NonNull final NetworkCallbackOne<List<CaredUser>> callback) {
        if (queueIfNotAuthedYet(() -> getCaredList(callback))) return;

        getRetrofitRequests().getCaredList(userToken).enqueue(getDefaultOneCallback(callback));
    }

    // Link

    public void tryToLinkCaretaker(@NonNull String code, @Nullable final NetworkCallbackOne<Integer> callback) {
        if (queueIfNotAuthedYet(() -> tryToLinkCaretaker(code, callback))) return;

        getRetrofitRequests().postLink(userToken, code).enqueue(getDefaultOneCallback(callback));
    }

    // Inner Logic

    private boolean processRequestIfRUidNotSet(NetworkCallback callback) {
        if (rUid == null || rUid.equals("")) {
            callback.onFailure(new Throwable("No rUid set!"));
            return true;
        } else return false;
    }
}
