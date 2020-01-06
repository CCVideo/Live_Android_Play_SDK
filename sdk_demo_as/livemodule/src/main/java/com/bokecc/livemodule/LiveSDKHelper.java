package com.bokecc.livemodule;

import android.app.Application;
import android.util.Log;

import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.replay.pojo.MyObjectBox;
import com.bokecc.sdk.mobile.live.util.HttpUtil;

import io.objectbox.BoxStore;

/**
 * 直播 SDK 帮助类
 */
public class LiveSDKHelper {

    private static final String TAG = "CCLive";

    /**
     * 初始化SDK
     *
     * @param app 应用上下文
     */
    public static void initSDK(Application app) {
        // 判断是否初始化了SDK，如果没有就进行初始化
        if (DWLiveEngine.getInstance() == null) {
            // 拉流 SDK 初始化
            DWLiveEngine.init(app, true);
            // 设置Http请求日志输出LEVEL为详细（其他设置字段请参考CCLiveDoc的API文档查看）
        } else {
            Log.i(TAG, "DWLiveEngine has init");
        }
    }


}
