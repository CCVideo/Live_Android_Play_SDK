package com.bokecc.dwlivedemo.popup;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bokecc.dwlivedemo.R;
import com.bokecc.sdk.mobile.live.util.DevicesUtil;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 悬浮弹出框（支持拖动）
 */
public class FloatingPopupWindow implements View.OnClickListener {
    private Activity mContext;
    private View mPopContentView;
    private RelativeLayout mFloatingLayout;
    private float lastX;
    private float lastY;
    private AtomicBoolean isShow = new AtomicBoolean(false);
    // 删除按钮 暂不显示
    private ImageView mDismissView;

    public FloatingPopupWindow(Activity context) {
        mContext = context;
    }

    /**
     * 是否显示
     */
    public boolean isShowing() {
        return isShow.get();
    }

    /**
     * 添加新View
     */
    public void addView(View view) {
        if (mFloatingLayout != null) {
            mFloatingLayout.addView(view);
        }
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
        isShow.set(true);
        mPopContentView = LayoutInflater.from(mContext).inflate(R.layout.popup_window_floating, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(300, 224);
        mPopContentView.setTranslationX(0);
        mPopContentView.setTranslationY(DevicesUtil.getDeviceScreenHeight(mContext) / 3);
        ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).addView(mPopContentView, layoutParams);
        mFloatingLayout = mPopContentView.findViewById(R.id.floating_layout);
        mPopContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        mPopContentView.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - lastX);
                        int deltaY = (int) (event.getRawY() - lastY);
                        float transX = mPopContentView.getX() + deltaX;
                        float transY = mPopContentView.getY() + deltaY;
                        //判断是否超过屏幕外
                        if (transX < 0) {
                            transX = 0;
                        }
                        if (transX > (DevicesUtil.getDeviceScreenWidth(mContext) - mPopContentView.getWidth())) {
                            transX = (DevicesUtil.getDeviceScreenWidth(mContext) - mPopContentView.getWidth());
                        }
                        if (transY < 0) {
                            transY = 0;
                        }
                        if (transY > (DevicesUtil.getDeviceScreenHeight(mContext) - mPopContentView.getHeight())) {
                            transY = (DevicesUtil.getDeviceScreenHeight(mContext) - mPopContentView.getHeight());
                        }
                        mPopContentView.setTranslationX(transX);
                        mPopContentView.setTranslationY(transY);
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        break;
                }
                return true;
            }
        });

        mDismissView = new ImageView(mContext);
        mDismissView.setImageResource(R.drawable.live_screen_close);
        mDismissView.setOnClickListener(this);
        mDismissView.setVisibility(View.GONE);
    }

    /**
     * 隐藏弹出框
     */
    public void dismiss() {
        if (mFloatingLayout != null) {
            if (isShowing()) {
                ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).removeView(mPopContentView);
                isShow.set(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    /**
     * 横竖屏切换  调整连麦窗口的位置
     *
     * @param newConfig
     */
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mPopContentView != null) {
                mPopContentView.setTranslationX(0);
                float y = DevicesUtil.getDeviceScreenWidth(mContext) / 3;
                if (y < 0) {
                    y = 0;
                }
                if (y > (DevicesUtil.getDeviceScreenWidth(mContext) - mPopContentView.getHeight())) {
                    y = (DevicesUtil.getDeviceScreenWidth(mContext) - mPopContentView.getHeight());
                }
                mPopContentView.setTranslationY(y);
            }
        } else {
            if (mPopContentView != null) {
                mPopContentView.setTranslationX(0);
                float y = DevicesUtil.getDeviceScreenHeight(mContext) / 3;
                if (y < 0) {
                    y = 0;
                }
                if (y > (DevicesUtil.getDeviceScreenHeight(mContext) - mPopContentView.getHeight())) {
                    y = (DevicesUtil.getDeviceScreenHeight(mContext) - mPopContentView.getHeight());
                }
                mPopContentView.setTranslationY(y);
            }
        }
    }
}
