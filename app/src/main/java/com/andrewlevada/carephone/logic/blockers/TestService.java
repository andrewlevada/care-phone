package com.andrewlevada.carephone.logic.blockers;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.andrewlevada.carephone.Toolbox;

public class TestService extends Service {

    CountDownTimer cdt = null;
    Intent i;

    @Override
    public void onCreate() {
        Toolbox.fastLog("initing 999999999999999999");

        i = new Intent(this, TestService.class);
        startService(i);
        cdt = new CountDownTimer(20000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Toolbox.fastLog("dfghjkhgftrdtfgyhjkjhgytyghjkl");
            }
        };
        cdt.start();

        if(Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(this, Blocker_O.class));
        } else {
            Intent i;
            i = new Intent(this, Blocker_O.class);
            ContextCompat.startForegroundService(this, i);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}