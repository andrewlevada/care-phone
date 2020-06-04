package com.andrewlevada.carephone.logic.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.PhoneNumber;
import com.andrewlevada.carephone.logic.StatisticsPack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network {
    private static Network instance;
    private static RetrofitRequests retrofitRequests;

    private String userToken;
    private ExtendedAuthTokenCallback authTokenCallback;

    // Config

    public void useFirebaseAuthToken() {
        authTokenCallback = new ExtendedAuthTokenCallback() {
            private List<Runnable> callbacks;

            @Override
            public void onGenerated(String token) {
                userToken = token;
                authTokenCallback = null;
                if (callbacks != null) for (Runnable callback: callbacks)
                    if (callback != null) new Thread(callback).start();
            }

            public void addCallback(Runnable callback) {
                if (callbacks == null) callbacks = new ArrayList<>();
                callbacks.add(callback);
            }
        };

        Toolbox.requestFirebaseAuthToken(authTokenCallback);
    }

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

    // Cared List

    public void getCaredList(@NonNull final NetworkCallbackOne<List<String>> callback) {
        if (queueIfNotAuthedYet(() -> getCaredList(callback))) return;

        getRetrofitRequests().getCaredList(userToken).enqueue(getDefaultOneCallback(callback));
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

    public void tryToLinkCaretaker(@NonNull String code, @Nullable final NetworkCallbackOne<Integer> callback) {
        if (queueIfNotAuthedYet(() -> tryToLinkCaretaker(code, callback))) return;

        getRetrofitRequests().postLink(userToken,code).enqueue(getDefaultOneCallback(callback));
    }

    // Private Logic

    private boolean queueIfNotAuthedYet(Runnable callback) {
        if (userToken != null) return false;

        if (authTokenCallback == null) useFirebaseAuthToken();
        authTokenCallback.addCallback(callback);

        return true;
    }

    private RetrofitRequests getRetrofitRequests() {
        if (retrofitRequests == null) retrofitRequests = new Retrofit.Builder()
                .baseUrl(Config.baseNetworkUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitRequests.class);

        return retrofitRequests;
    }

    private Callback<Void> getDefaultVoidCallback(NetworkCallbackZero callback) {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    if (response.errorBody() != null) Toolbox.FastLog("--------------- SERVER ERROR: " + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toolbox.FastLog("--------------- SERVER ERROR T: " + t.getMessage());
                if (callback != null) callback.onFailure(t);
            }
        };
    }

    private <T> Callback<T> getDefaultOneCallback(NetworkCallbackOne<T> callback) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                try {
                    if (response.errorBody() != null) Toolbox.FastLog("--------------- SERVER ERROR: " + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (response.body() != null) callback.onSuccess(response.body());
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Toolbox.FastLog("--------------- SERVER ERROR T: " + t.getMessage());
                callback.onFailure(t);
            }
        };
    }

    public static Network getInstance() {
        if (instance == null) instance = new Network();
        return instance;
    }

    public interface NetworkCallbackZero {
        void onSuccess();
        void onFailure(@Nullable Throwable throwable);
    }
    public interface NetworkCallbackOne<T> {
        void onSuccess(T arg);
        void onFailure(@Nullable Throwable throwable);
    }

    private interface ExtendedAuthTokenCallback extends Toolbox.AuthTokenCallback {
        void addCallback(Runnable callback);
    }
}
