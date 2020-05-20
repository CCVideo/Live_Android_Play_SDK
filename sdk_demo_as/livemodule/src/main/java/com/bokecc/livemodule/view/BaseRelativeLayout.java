package com.bokecc.livemodule.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * RelativeLayout基类，提供通用的工具方法
 */
public abstract class BaseRelativeLayout extends RelativeLayout {

    public Context mContext;

    public BaseRelativeLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public BaseRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    public BaseRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initViews();
    }

    // 初始化相关View
    public abstract void initViews();

    //***************************************** 工具方法 *****************************************

    // 在Ui线程上进行吐司提示
    public void toastOnUiThread(final String msg) {
        // 判断是否处在UI线程
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(msg);
                }
            });
        } else {
            showToast(msg);
        }
    }

    // 在UI线程执行一些操作
    public void runOnUiThread(Runnable runnable) {
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }

    // 进行吐司提示
    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    // 判断当前的线程是否是UI线程
    protected boolean checkOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
