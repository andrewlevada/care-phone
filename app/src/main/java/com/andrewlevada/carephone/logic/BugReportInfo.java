package com.andrewlevada.carephone.logic;

import android.os.Build;

import androidx.annotation.NonNull;

public class BugReportInfo {
    public int sdkNum;
    public String versionCodeName;
    public String deviceModel;

    private BugReportInfo() {
        sdkNum = Build.VERSION.SDK_INT;
        versionCodeName = Build.VERSION.RELEASE;
        deviceModel = Build.BRAND + " : " + Build.MODEL;
    }

    public static BugReportInfo generate() {
        return new BugReportInfo();
    }

    @Override @NonNull
    public String toString() {
        return "{sdkNum:" + sdkNum +
                ",versionCodeName:" + versionCodeName +
                ",deviceModel:" + deviceModel +
                '}';
    }
}
