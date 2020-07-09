package com.andrewlevada.carephone.logic.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.andrewlevada.carephone.Config;
import com.andrewlevada.carephone.logic.LogRecord;
import com.andrewlevada.carephone.logic.localdb.LocalDatabase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.Executors;

class LocalNetworkProxy {
    private static LocalNetworkProxy instance;
    private LocalDatabase database;

    // Log

    public void addToLog(@NonNull LogRecord logRecord, @Nullable final Network.NetworkCallbackZero callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            database.getLogRecordsDao().insert(logRecord);
            if (callback != null) callback.onSuccess();
        });
    }

    public void getLog(int limit, int offset, @NonNull final Network.NetworkCallbackOne<List<LogRecord>> callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Message message = new Message();
            message.obj = database.getLogRecordsDao().getAllPeopleWithFavoriteColor(limit, offset);
            new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    callback.onSuccess((List<LogRecord>) msg.obj);
                }
            }.sendMessage(message);
        });
    }

    // Private methods

    void initDatabase(Context appContext) {
        if (database != null) return;

        String postfix = FirebaseAuth.getInstance().getCurrentUser() != null ?
                String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getUid().hashCode()) : "";

        database = Room.databaseBuilder(appContext,
                LocalDatabase.class, Config.localDatabaseName + postfix).build();
    }

    static LocalNetworkProxy getInstance() {
        if (instance == null) instance = new LocalNetworkProxy();
        return instance;
    }
}
