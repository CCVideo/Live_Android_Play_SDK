package com.bokecc.livemodule.live;

/**
 * 直播间信息回调监听
 */
public interface DWLiveRoomListener {

    /**
     * 切换视频文档区域
     *
     * @param isVideoMain 视频是否为主区域
     */
    void onSwitchVideoDoc(boolean isVideoMain);

    /**
     * 展示直播间标题
     */
    void showRoomTitle(String title);

    /**
     * 展示直播间人数
     */
    void showRoomUserNum(int number);

    /**
     * 踢出用户
     */
    void onKickOut();
}
