package com.bokecc.livemodule;

import android.content.Context;
import android.util.Log;

import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.logging.LogHelper;
import com.bokecc.sdk.mobile.live.util.HttpUtil;

/**
 * 直播 SDK 帮助类
 */
public class LiveSDKHelper {

    private static final String TAG = "CCLive";

    /**
     * 初始化SDK
     * @param context 应用上下文
     */
    public static void initSDK(Context context) {
        // 判断是否初始化了SDK，如果没有就进行初始化
        if (DWLiveEngine.getInstance() == null) {
            context = context.getApplicationContext();
            // 拉流 SDK 初始化
            DWLiveEngine.init(context);
            // 初始化日志记录模块
            LogHelper.getInstance().init(context, true, null);
            // 设置Http请求日志输出LEVEL为详细（其他设置字段请参考CCLiveDoc的API文档查看）
            HttpUtil.LOG_LEVEL = HttpUtil.HttpLogLevel.GENERAL;
        } else {
            Log.i(TAG, "DWLiveEngine has init");
        }
    }
}
