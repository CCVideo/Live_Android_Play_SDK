package com.bokecc.livemodule.live;

/**
 * 直播间连麦回调监听
 */
public interface DWLiveRTCListener {

    /**
     * 主播端接通连麦,开始
     * 是否需要自己调整视频窗口大小
     * @param isVideoRtc 当前连麦是否是视频连麦
     * @param videoSize 视频的宽高，值为"600x400"
     */
    void onEnterSpeak(boolean isVideoRtc, boolean needAdjust, final String videoSize);

    /**
     * 连麦断开
     */
    void onDisconnectSpeak();

    /**
     * 连麦异常
     */
    void onSpeakError(Exception e);
}
