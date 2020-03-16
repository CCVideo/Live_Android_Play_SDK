package com.bokecc.dwlivedemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.bokecc.livemodule.LiveSDKHelper;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.concurrent.TimeUnit;

import cn.dreamtobe.filedownloader.OkHttp3Connection;
import okhttp3.OkHttpClient;

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
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(20_000, TimeUnit.SECONDS); // customize t
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new OkHttp3Connection.Creator(builder))
                .commit();
//      初始化SDK
        LiveSDKHelper.initSDK(this);
    }


    public static Context getContext() {
        return context;
    }
}

