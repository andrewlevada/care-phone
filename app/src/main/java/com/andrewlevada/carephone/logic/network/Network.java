package com.andrewlevada.carephone.logic.network;

import androidx.annotation.Nullable;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.Toolbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network {
    private static final String LOG_ERROR_PREFIX = "--------------- SERVER ERROR: ";
    private static final String LOG_PREFIX = "SERVER: ";

    private static Network instance;
    private static RetrofitRequests retrofitRequests;
    private ExtendedAuthTokenCallback authTokenCallback;
    String userToken;

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

    boolean queueIfNotAuthedYet(Runnable callback) {
        if (userToken != null) return false;

        if (authTokenCallback == null) useFirebaseAuthToken();
        authTokenCallback.addCallback(callback);

        return true;
    }

    RetrofitRequests getRetrofitRequests() {
        if (retrofitRequests == null) retrofitRequests = new Retrofit.Builder()
                .baseUrl(Config.baseNetworkUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitRequests.class);

        return retrofitRequests;
    }

    Callback<Void> getDefaultVoidCallback(NetworkCallbackZero callback) {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toolbox.fastLog(response.code() + " with " + response.body());
                if (response.errorBody() != null)
                    try {
                        Toolbox.fastLog("err: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toolbox.fastLog(LOG_ERROR_PREFIX + t.getMessage());
                if (callback != null) callback.onFailure(t);
            }
        };
    }
    <T> Callback<T> getDefaultOneCallback(NetworkCallbackOne<T> callback) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Toolbox.fastLog(response.code() + " with " + response.body());
                if (response.errorBody() != null)
                    try {
                        Toolbox.fastLog("err: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                if (response.body() != null) callback.onSuccess(response.body());
                else callback.onFailure(null);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Toolbox.fastLog(LOG_ERROR_PREFIX + t.getMessage());
                callback.onFailure(t);
            }
        };
    }

    public static Network config() {
        if (instance == null) instance = new Network();
        return instance;
    }
    public static NetworkCared cared() {
        if (NetworkCared.instance == null) NetworkCared.instance = new NetworkCared();
        return NetworkCared.instance;
    }
    public static NetworkCaretaker caretaker() {
        if (NetworkCaretaker.instance == null) NetworkCaretaker.instance = new NetworkCaretaker();
        return NetworkCaretaker.instance;
    }
    public static NetworkBothRouter router() {
        if (NetworkBothRouter.instance == null) NetworkBothRouter.instance = new NetworkBothRouter();
        return NetworkBothRouter.instance;
    }

    public interface NetworkCallback {
        void onFailure(@Nullable Throwable throwable);
    }
    public interface NetworkCallbackZero extends NetworkCallback {
        void onSuccess();
    }
    public interface NetworkCallbackOne<T> extends NetworkCallback {
        void onSuccess(T arg);
    }

    private interface ExtendedAuthTokenCallback extends Toolbox.AuthTokenCallback {
        void addCallback(Runnable callback);
    }
}
