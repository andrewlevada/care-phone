package com.andrewlevada.carephone.logic;

import android.os.Build;

import androidx.annotation.NonNull;

import com.andrewlevada.carephone.BuildConfig;

public class BugReportInfo {
    public int sdkNum;
    public int versionCode;
    public String deviceModel;

    private BugReportInfo() {
        sdkNum = Build.VERSION.SDK_INT;
        versionCode = BuildConfig.VERSION_CODE;
        deviceModel = Build.BRAND + " : " + Build.MODEL;
    }

    public static BugReportInfo generate() {
        return new BugReportInfo();
    }

    @Override @NonNull
    public String toString() {
        return "{sdkNum:" + sdkNum +
                ",versionCode:" + versionCode +
                ",deviceModel:" + deviceModel +
                '}';
    }
}
