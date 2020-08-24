package com.bokecc.dwlivedemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.bokecc.livemodule.LiveSDKHelper;
import com.bokecc.sdk.mobile.live.logging.FwLog;
import com.tencent.bugly.crashreport.CrashReport;

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
        //初始化bugly
        CrashReport.initCrashReport(getApplicationContext(), "860d2d72ac", false);

        FwLog.setConsoleLogLevel(4);

    }


    public static Context getContext() {
        return context;
    }
}

