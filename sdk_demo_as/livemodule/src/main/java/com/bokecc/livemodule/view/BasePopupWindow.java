package com.bokecc.livemodule.view;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.PopupWindow;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.DensityUtil;

public abstract class BasePopupWindow implements View.OnClickListener {

    protected Context mContext;

    View mOutsideView;
    View mAnimView;

    private PopupWindow mPopupWindow;
    private boolean isOutsideCancel = false;
    private boolean isDismissing = false; // 正在dismiss
    private boolean isKeyBackCancel = false;

    private OnPopupDismissListener mOnPopupDismissListener;
    private View mPopContentView;

    /**
     * @param context 上下文
     */
    public BasePopupWindow(Context context) {
        this(context, 0, 0);
    }

    /**
     * @param context 上下文
     * @param width   可见区域的宽度 单位dp
     * @param height  可见区域的高度
     */
    public BasePopupWindow(Context context, int width, int height) {
        mContext = context;

        mPopContentView = LayoutInflater.from(mContext).inflate(getContentView(), null);
        mOutsideView = findViewById(R.id.id_popup_window_outside_view);
        mAnimView = findViewById(R.id.id_popup_window_anim_view);

        // 适配横竖屏
        ViewGroup.LayoutParams params = mAnimView.getLayoutParams();
        if (width > 0) {
            params.width = DensityUtil.dp2px(mContext, width);
        }
        if (height > 0) {
            params.height = DensityUtil.dp2px(mContext, height);
        }
        mAnimView.setLayoutParams(params);
        mOutsideView.setClickable(true);
        mOutsideView.setOnClickListener(this);
        mAnimView.setOnClickListener(this); // 主要作用是拦截内容区域点击事件
        mPopupWindow = new PopupWindow(mPopContentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        configPopupWindow();
        onViewCreated();
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
        mPopContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (isKeyBackCancel) {
                        dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    protected <T extends View> T findViewById(int id) {
        return (T) mPopContentView.findViewById(id);
    }

    /**
     * 设置点击popupwindow外部是否可以取消
     */
    public void setOutsideCancel(boolean flag) {
        isOutsideCancel = flag;
    }

    /**
     * 设置popupwindow外部背景颜色
     */
    public void setOutsideBackgroundColor(int color) {
        mOutsideView.setBackgroundColor(color);
    }

    /**
     * 设置点击返回键是否可以消失
     */
    public void setBackPressedCancel(boolean flag) {
        if (flag) {
            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        } else {
            mPopupWindow.setBackgroundDrawable(null);
        }
    }

    public void setKeyBackCancel(boolean flag) {
        isKeyBackCancel = flag;
    }

    /**
     * 是否显示
     */
    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    public void setOnPopupDismissListener(OnPopupDismissListener onPopupDismissListener) {
        mOnPopupDismissListener = onPopupDismissListener;
    }

    /** 监听PopupWindow的消失事件·(真) */
    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        mPopupWindow.setOnDismissListener(onDismissListener);
    }

    /**
     * 显示
     */
    public void show(View view) {
        if (isShowing()) {
            return;
        }
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        mAnimView.startAnimation(getEnterAnimation());
    }

    /**
     * 隐藏
     */
    public void dismiss() {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                Animation animation = getExitAnimation();
                animation.setAnimationListener(new PopupAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPopupWindow.dismiss();
                        isDismissing = false;
                        if (mOnPopupDismissListener != null) {
                            mOnPopupDismissListener.onDismiss();
                        }
                    }
                });
                mAnimView.startAnimation(animation);
            }
        }
    }

    /**
     * 立刻隐藏
     */
    public void dismissImmediate() {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
                isDismissing = false;
                if (mOnPopupDismissListener != null) {
                    mOnPopupDismissListener.onDismiss();
                }
            }
        }
    }

    protected abstract void onViewCreated();

    protected abstract int getContentView();

    protected abstract Animation getEnterAnimation();

    protected abstract Animation getExitAnimation();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.id_popup_window_outside_view) {
            if (isOutsideCancel) {
                if (!isDismissing) {
                    isDismissing = true;
                    dismiss();
                }
            }
        }
    }

    public interface OnPopupWindowDismissListener {
        void onDismiss();
    }

    private abstract class PopupAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            // ignore
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // ignore
        }

    }

    public interface OnPopupDismissListener {

        /**
         * 消失后调用
         */
        void onDismiss();
    }



}