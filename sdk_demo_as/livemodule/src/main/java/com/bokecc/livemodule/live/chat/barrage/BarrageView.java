package com.bokecc.livemodule.live.chat.barrage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

/**
 * 弹幕组件
 * @author cc视频
 *
 */
public class BarrageView extends TextView {

    private boolean isFinish = false;

    public BarrageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BarrageView(Context context) {
        super(context);
    }

    public BarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("NewApi")
    public void move(float fromX, float toX, float fromY, float toY, long duration) { // toX参数没有使用

        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        this.measure(w, h);
        int width = this.getMeasuredWidth();

        Animation anim = new TranslateAnimation(fromX, width * -1 - 50, fromY, toY);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                isFinish = true;
            }
        });

        this.startAnimation(anim);
    }

    public boolean getFinish() {
        return isFinish;
    }

}