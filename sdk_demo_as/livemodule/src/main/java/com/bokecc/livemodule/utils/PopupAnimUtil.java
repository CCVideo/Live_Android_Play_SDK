package com.bokecc.livemodule.utils;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class PopupAnimUtil {

    private PopupAnimUtil() {
        throw new UnsupportedOperationException();
    }

    public static Animation getDefScaleEnterAnim() {
        Animation scaleAnim =
                new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(300);
        scaleAnim.setInterpolator(new AccelerateInterpolator());
        scaleAnim.setFillEnabled(true);
        scaleAnim.setFillAfter(true);
        return scaleAnim;
    }

    public static Animation getDefScaleExitAnim() {
        Animation scaleAnim =
                new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(300);
        scaleAnim.setInterpolator(new AccelerateInterpolator());
        scaleAnim.setFillEnabled(true);
        scaleAnim.setFillAfter(true);
        return scaleAnim;
    }

    public static Animation getDefTranslateEnterAnim() {
        Animation translateAnim = new TranslateAnimation(
                1f, 1f, 0f, 1f
        );
        translateAnim.setDuration(300);
        translateAnim.setInterpolator(new AccelerateInterpolator());
        translateAnim.setFillEnabled(true);
        translateAnim.setFillAfter(true);
        return translateAnim;
    }

    public static Animation getDefTranslateExitAnim() {
        Animation translateAnim = new TranslateAnimation(
                1f, 1f, 1f, 0f
        );
        translateAnim.setDuration(300);
        translateAnim.setInterpolator(new AccelerateInterpolator());
        translateAnim.setFillEnabled(true);
        translateAnim.setFillAfter(true);
        return translateAnim;
    }
}
