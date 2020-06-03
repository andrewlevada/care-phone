package com.andrewlevada.carephone.logic.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;
import com.andrewlevada.carephone.logic.PhoneNumber;

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

    public void syncWhitelist(@NonNull final NetworkCallbackOne<List<PhoneNumber>> callback) {
        if (queueIfNotAuthedYet(() -> syncWhitelist(callback))) return;

        getRetrofitRequests().getWhitelist(userToken).enqueue(new Callback<List<PhoneNumber>>() {
            @Override
            public void onResponse(Call<List<PhoneNumber>> call, Response<List<PhoneNumber>> response) {
                if (response.body() != null) callback.onSuccess(response.body());
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(Call<List<PhoneNumber>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
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

    private boolean queueIfNotAuthedYet(Runnable callback) {
        if (authTokenCallback != null) {
            authTokenCallback.addCallback(callback);
            return true;
        }
        return false;
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
                    if (response.errorBody() != null) Toolbox.FastLog("RESPONSE ERROR: " + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (callback != null) callback.onFailure(t);
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
