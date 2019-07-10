package com.bokecc.dwlivemoduledemo.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.sdk.mobile.live.logging.ELog;

/**
 * 退出确认弹出框
 */
public class ExitPopupWindow {

    protected Context mContext;

    private PopupWindow mPopupWindow;

    private View mPopContentView;

    /**
     * @param context 上下文
     */
    public ExitPopupWindow(Context context) {
        this(context, 0, 0);
    }

    /**
     * @param context 上下文
     * @param width   可见区域的宽度 单位dp
     * @param height  可见区域的高度
     */
    public ExitPopupWindow(Context context, int width, int height) {
        mContext = context;
        mPopContentView = LayoutInflater.from(mContext).inflate(R.layout.popup_window_exit, null);
        mPopupWindow = new PopupWindow(mPopContentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initButtons();
        configPopupWindow();
    }

    private void initButtons() {
        mPopContentView.findViewById(R.id.cancel_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mPopContentView.findViewById(R.id.confirm_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmExitRoomListener != null) {
                    confirmExitRoomListener.onConfirmExitRoom();
                }
            }
        });
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
    private boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    /**
     * 显示弹出框
     */
    public void show(View view) {
        ELog.d(ExitPopupWindow.class,"ExitPop show() : isShowing():"+isShowing());
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

    private ConfirmExitRoomListener confirmExitRoomListener;

    /**
     * 设置退出监听
     *
     * @param listener 监听
     */
    public void setConfirmExitRoomListener(ConfirmExitRoomListener listener) {
        confirmExitRoomListener = listener;
    }

    // 退出直播间监听
    public interface ConfirmExitRoomListener {
        /**
         * 退出直播间
         */
        void onConfirmExitRoom();
    }
}
