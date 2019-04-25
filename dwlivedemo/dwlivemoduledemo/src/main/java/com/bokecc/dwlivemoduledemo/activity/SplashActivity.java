package com.bokecc.dwlivemoduledemo.activity;

import android.os.Bundle;
import android.os.Handler;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.activity.PilotActivity;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;

/**
 * 引导页
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        requestFullScreenFeature();
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