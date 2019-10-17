package com.bokecc.livemodule.live;

/**
 * 连麦状态监听
 */
public interface DWLiveRTCStatusListener {

    /**
     * 通知进入连麦状态
     */
    void onEnterRTC(boolean isVideoRtc);

    /**
     * 通知退出连麦状态
     */
    void onExitRTC();

    /**
     * 通知主播端关闭连麦
     */
    void onCloseSpeak();
}
