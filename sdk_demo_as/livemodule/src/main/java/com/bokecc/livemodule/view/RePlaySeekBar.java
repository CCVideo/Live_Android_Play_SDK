package com.bokecc.livemodule.view;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 回放界面的seekbar  当视频未加载的时候不可拖拽
 */
public class RePlaySeekBar extends AppCompatSeekBar {
    public RePlaySeekBar(Context context) {
        super(context);
    }

    public RePlaySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RePlaySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCanSeek) {
            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }

    private boolean isCanSeek = false;

    public void setCanSeek(boolean canSeek) {
        isCanSeek = canSeek;
    }

    public boolean isCanSeek() {
        return isCanSeek;
    }
}
