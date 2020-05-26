package com.andrewlevada.carephone.logic.blockers;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;

public abstract class Blocker {
    Context context;

    public abstract void initiateBlocking();
    public abstract void onDestroy();

    @Nullable
    public static Blocker getSuitableVersion(Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) return new Blocker_N_MR1(context);
        else return null;
    }

    Blocker(Context context) {
        this.context = context;
    }
}
