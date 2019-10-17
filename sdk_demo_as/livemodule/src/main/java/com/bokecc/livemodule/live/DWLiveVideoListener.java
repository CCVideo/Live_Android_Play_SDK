package com.bokecc.livemodule.live;

import com.bokecc.sdk.mobile.live.DWLive;

/**
 * 直播视频回调监听
 */
public interface DWLiveVideoListener {

    /**
     * 流结束
     *
     * @param isNormal 流是否正常结束
     */
    void onStreamEnd(boolean isNormal);

    /**
     * 播放状态
     *
     * @param status 包括PLAYING, PREPARING共2种状态
     */
    void onLiveStatus(DWLive.PlayStatus status);

    /*
     * 禁播
     *
     * @param reason
     */
    void onBanStream(String reason);

    /**
     * 解禁
     */
    void onUnbanStream();

}
