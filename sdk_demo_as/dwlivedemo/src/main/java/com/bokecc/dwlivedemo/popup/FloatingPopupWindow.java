package com.bokecc.dwlivedemo.popup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bokecc.dwlivedemo.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 悬浮弹出框（支持拖动）
 */
public class FloatingPopupWindow implements View.OnClickListener {
    private Activity mContext;
    private RelativeLayout mFloatingLayout;
    private float lastX;
    private float lastY;
    private AtomicBoolean isShow = new AtomicBoolean(false);

    // 当前容器里面装载的View
    private View mNowView;
    // 删除按钮
    private ImageView ivClose;
    private Handler handler = new Handler(Looper.getMainLooper());

    public FloatingPopupWindow(Activity context) {
        mContext = context;
        mFloatingLayout = new RelativeLayout(mContext);
        mFloatingLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorBack));
        mFloatingLayout.setTranslationX(0);
        mFloatingLayout.setTranslationY(getDeviceScreenHeight(mContext) / 3f);

        ivClose = new ImageView(mContext);
        ivClose.setImageResource(R.drawable.live_screen_close);
    }

    // region -------------------- 对外提供方法-----------------------------------------------------

    /**
     * 添加新View
     */
    public void addView(View view) {
        mNowView = view;
        if (mFloatingLayout != null) {
            mFloatingLayout.addView(view, 0);
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
    @SuppressLint("ClickableViewAccessibility")
    public void show(View view) {
        if (isShowing()) {
            return;
        }
        isShow.set(true);
        ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).removeView(mFloatingLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(dp2px(mContext, 120), dp2px(mContext, 90));
        ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).addView(mFloatingLayout, layoutParams);
        mFloatingLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        mFloatingLayout.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - lastX);
                        int deltaY = (int) (event.getRawY() - lastY);
                        float transX = mFloatingLayout.getX() + deltaX;
                        float transY = mFloatingLayout.getY() + deltaY;
                        //判断是否超过屏幕外
                        if (transX < 0) {
                            transX = 0;
                        }
                        if (transX > (getDeviceScreenWidth(mContext) - mFloatingLayout.getWidth())) {
                            transX = (getDeviceScreenWidth(mContext) - mFloatingLayout.getWidth());
                        }
                        if (transY < 0) {
                            transY = 0;
                        }
                        if (transY > (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight())) {
                            transY = (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight());
                        }
                        mFloatingLayout.setTranslationX(transX);
                        mFloatingLayout.setTranslationY(transY);
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        if (ivClose.getVisibility() == View.GONE) {
                            ivClose.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.postDelayed(runnable, 3000);
                        break;
                }
                return true;
            }
        });
        mFloatingLayout.removeView(ivClose);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(mContext, 20), dp2px(mContext, 20));
        lp.addRule(RelativeLayout.ALIGN_PARENT_END, 1);
        mFloatingLayout.addView(ivClose, lp);
        ivClose.setVisibility(View.GONE);

        ivClose.setOnClickListener(this);
    }

    /**
     * 隐藏弹出框
     */
    public void dismiss() {
        handler.removeCallbacks(runnable);
        if (mFloatingLayout != null) {
            if (isShowing()) {
                try {
                    ((ViewGroup) mContext.getWindow().getDecorView().getRootView()).removeView(mFloatingLayout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isShow.set(false);
                if (floatDismissListener != null) {
                    floatDismissListener.dismiss();
                }
            }
        }
    }

    /**
     * 横竖屏切换  调整连麦窗口的位置
     *
     * @param orientation 屏幕方向 1:竖屏
     *                    {@link Configuration}
     */
    public void onConfigurationChanged(int orientation) {
        if (!isShowing()) return;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE.value) {
            if (mFloatingLayout != null) {
                mFloatingLayout.setTranslationX(0);
                float y = getDeviceScreenWidth(mContext) / 3f;
                if (y < 0) {
                    y = 0;
                }
                if (y > (getDeviceScreenWidth(mContext) - mFloatingLayout.getHeight())) {
                    y = (getDeviceScreenWidth(mContext) - mFloatingLayout.getHeight());
                }
                mFloatingLayout.setTranslationY(y);
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT.value) {
            if (mFloatingLayout != null) {
                mFloatingLayout.setTranslationX(0);
                float y = getDeviceScreenHeight(mContext) / 3f;
                if (y < 0) {
                    y = 0;
                }
                if (y > (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight())) {
                    y = (getDeviceScreenHeight(mContext) - mFloatingLayout.getHeight());
                }
                mFloatingLayout.setTranslationY(y);
            }
        }
    }


    /**
     * 获取当前正展示的View
     */
    public View getNowView() {
        return mNowView;
    }

    /**
     * 是否显示
     */
    public boolean isShowing() {
        return isShow.get();
    }

    //endregion

    // region -------------------- 私有方法---------------------------------------------------------
    // 屏幕方向
    public enum Configuration {
        ORIENTATION_PORTRAIT(1), ORIENTATION_LANDSCAPE(2);

        public int value;

        Configuration(int i) {
            value = i;
        }
    }

    // dp-->px
    private int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    // 获取设备屏幕高度
    private int getDeviceScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    // 获取设备屏幕宽度
    public static int getDeviceScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    //endregion
    @Override
    public void onClick(View v) {
        dismiss();
    }

    private FloatDismissListener floatDismissListener;

    public void setFloatDismissListener(FloatDismissListener floatDismissListener) {
        this.floatDismissListener = floatDismissListener;
    }

    public interface FloatDismissListener {
        void dismiss();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (ivClose != null) {
                ivClose.setVisibility(View.GONE);
            }

        }
    };
}
