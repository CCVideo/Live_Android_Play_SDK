package com.bokecc.dwlivedemo.activity;

import android.os.Bundle;
import android.os.Handler;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.utils.StatusBarUtil;

/**
 * 引导页
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        //requestFullScreenFeature();
        // 沉浸式
        StatusBarUtil.transparencyBar(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 3 秒后跳转到导航页
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                go(PilotActivity.class);
                finish();
            }
        }, 3 * 1000L);
    }

}