package com.andrewlevada.carephone.ui.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.andrewlevada.carephone.Toolbox;

public class LogScrollView extends ScrollView {
    private Toolbox.Callback bottomCallback;
    private View bottomView;

    public void setBottomCallback(Toolbox.Callback bottomCallback) {
        this.bottomCallback = bottomCallback;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (bottomView == null) bottomView = getChildAt(getChildCount() - 1);

        // If difference is zero, then the bottom has been reached
        int difference = (bottomView.getBottom() - getHeight() - getScrollY());
        if (difference == 0 && bottomCallback != null) bottomCallback.invoke();

        super.onScrollChanged(l, t, oldl, oldt);
    }

    public LogScrollView(Context context) {
        super(context);
    }
    public LogScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public LogScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public LogScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}