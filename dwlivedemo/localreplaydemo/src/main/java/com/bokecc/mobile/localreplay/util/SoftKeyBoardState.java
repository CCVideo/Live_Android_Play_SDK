package com.bokecc.mobile.localreplay.util;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class SoftKeyBoardState implements ViewTreeObserver.OnGlobalLayoutListener {

    private View mRoot;

    private boolean isSoftKeyBoardShow = false;

    private OnSoftKeyBoardStateChangeListener mListener;

    public SoftKeyBoardState(View root) {
        this(root, false);
    }

    public SoftKeyBoardState(View root, boolean isSoftKeyBoardShow) {
        mRoot = root;
        this.isSoftKeyBoardShow = isSoftKeyBoardShow;
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        mRoot.getWindowVisibleDisplayFrame(r);
        final int heightDiff = mRoot.getRootView().getHeight() - (r.bottom - r.top);
        if (!isSoftKeyBoardShow && heightDiff > mRoot.getRootView().getHeight() / 3) {
            isSoftKeyBoardShow = true;
            if (mListener != null) {
                mListener.onChange(true);
            }
        } else if (isSoftKeyBoardShow && heightDiff < mRoot.getRootView().getHeight() / 3) {
            isSoftKeyBoardShow = false;
            if (mListener != null) {
                mListener.onChange(false);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void release() {
        mRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public void setOnSoftKeyBoardStateChangeListener(OnSoftKeyBoardStateChangeListener listener) {
        mListener = listener;
    }

    public interface OnSoftKeyBoardStateChangeListener {
        void onChange(boolean isShow);
    }

}
