package com.bokecc.dwlivemoduledemo.popup;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.bokecc.dwlivemoduledemo.R;

/**
 * 悬浮弹出框（支持拖动）
 */
public class FloatingPopupWindow {

    // 弹窗宽度
    private static final int POPUP_WINDOW_WIDTH = 300;

    // 弹窗高度
    private static final int POPUP_WINDOW_HEIGHT = 225;

    private Context mContext;

    private PopupWindow mPopupWindow;

    private View mPopContentView;

    private RelativeLayout mFloatingLayout;

    private boolean IsDouble = false;
    private float lastX;
    private float lastY;

    public FloatingPopupWindow(Context context) {
        mContext = context;
        mPopContentView = LayoutInflater.from(mContext).inflate(R.layout.popup_window_floating, null);
        mPopupWindow = new PopupWindow(mPopContentView, POPUP_WINDOW_WIDTH, POPUP_WINDOW_HEIGHT);
        mFloatingLayout = mPopContentView.findViewById(R.id.floating_layout);
        mPopContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() == 1) {
                            IsDouble = false;
                        }
                        if (!IsDouble) {
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() > 1) {
                            IsDouble = true;
                        }
                        if (!IsDouble) {
                            int deltaX = (int) (event.getRawX() - lastX);
                            lastX = event.getRawX();
                            int deltaY = (int) (event.getRawY() - lastY);
                            lastY = event.getRawY();
                            mPopupWindow.update(deltaX + (int) lastX - (POPUP_WINDOW_WIDTH / 2), deltaY + (int) lastY - (POPUP_WINDOW_HEIGHT / 2), -1, -1, true);
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 是否显示
     */
    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    private View mNowView;  // 当前容器里面装载的View

    /**
     * 添加新View
     */
    public void addView(View view) {
        mNowView = view;
        if (mFloatingLayout != null) {
            mFloatingLayout.addView(view);
        }
    }

    /**
     * 获取当前正展示的View
     */
    public View getNowView() {
        return mNowView;
    }

    /**
     * 移除所有的子布局
     */
    public void removeAllView() {
        if (mFloatingLayout != null) {
            mFloatingLayout.removeAllViews();
        }
    }

    /**
     * 显示弹出框
     */
    public void show(View view) {
        if (isShowing()) {
            return;
        }
        mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 200);
    }

    /**
     * 隐藏弹出框
     */
    public void dismiss() {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        }
    }

}
