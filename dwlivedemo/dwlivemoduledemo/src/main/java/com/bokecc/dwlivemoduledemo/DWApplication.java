package com.bokecc.dwlivemoduledemo;

import android.app.Application;
import android.content.Context;

import com.bokecc.livemodule.LiveSDKHelper;

/**
 * 应用的 Application
 */
public class DWApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            context = this;
        }
        // 初始化SDK
        LiveSDKHelper.initSDK(getContext());
    }

    public static Context getContext() {
        return context;
    }
}

