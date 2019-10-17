package com.bokecc.dwlivemoduledemo.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.bokecc.dwlivemoduledemo.R;

public class LoginPopupWindow {

    protected Context mContext;

    private PopupWindow mPopupWindow;

    private View mPopContentView;

    /**
     * @param context 上下文
     */
    public LoginPopupWindow(Context context) {
        this(context, 0, 0);
    }

    /**
     * @param context 上下文
     * @param width   可见区域的宽度 单位dp
     * @param height  可见区域的高度
     */
    public LoginPopupWindow(Context context, int width, int height) {
        mContext = context;
        mPopContentView = LayoutInflater.from(mContext).inflate(R.layout.popup_window_login, null);
        mPopupWindow = new PopupWindow(mPopContentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        configPopupWindow();
    }


    /**
     * 配置PopupWindow
     */
    private void configPopupWindow() {
        mPopupWindow.setFocusable(true);
        // 点击空白区域
        mPopupWindow.setOutsideTouchable(false);
        //无需动画
        mPopupWindow.setAnimationStyle(0);
        // 拦截返回键
        mPopContentView.setFocusable(true);
        mPopContentView.setFocusableInTouchMode(true);
    }


    /**
     * 设置点击返回键是否可以消失
     */
    @Deprecated
    public void setBackPressedCancel(boolean flag) {
        if (flag) {
            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        } else {
            mPopupWindow.setBackgroundDrawable(null);
        }
    }

    /**
     * 是否显示
     */
    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    /**
     * 显示弹出框
     */
    public void show(View view) {
        if (isShowing()) {
            return;
        }
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
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
