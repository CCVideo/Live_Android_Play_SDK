package com.bokecc.dwlivedemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.bokecc.livemodule.LiveSDKHelper;

/**
 * 应用的 Application
 */
public class DWApplication extends Application {
    private static final String TAG = "DWApplication";
    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        if (context == null) {
            context = this;
        }
        //      初始化SDK
        LiveSDKHelper.initSDK(this);
    }


    public static Context getContext() {
        return context;
    }
}

