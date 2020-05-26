package com.andrewlevada.carephone.logic;

import android.content.Context;

import com.andrewlevada.carephone.logic.blockers.Blocker;

public class BlockerController {
    private Blocker blocker;

    public void StartBlocking() {
        blocker.initiateBlocking();
    }

    public BlockerController(Context context) {
        blocker = Blocker.getSuitableVersion(context);
        if (blocker == null) {
            // BLOCKING NOT SUPPORTED FOR PHONE'S VERSION OF ANDROID
        }
    }
}
