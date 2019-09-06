package com.bokecc.dwlivemoduledemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bokecc.livemodule.LiveSDKHelper;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.TimeUnit;

import cn.dreamtobe.filedownloader.OkHttp3Connection;
import cn.dreamtobe.threaddebugger.IThreadDebugger;
import cn.dreamtobe.threaddebugger.ThreadDebugger;
import cn.dreamtobe.threaddebugger.ThreadDebuggers;
import okhttp3.OkHttpClient;

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
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(20_000, TimeUnit.SECONDS); // customize t
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new OkHttp3Connection.Creator(builder))
                .commit();
        // 初始化SDK
        LiveSDKHelper.initSDK(this);
    }




    public static Context getContext() {
        return context;
    }
}

