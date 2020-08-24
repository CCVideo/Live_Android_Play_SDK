package com.bokecc.livemodule.live;

import com.bokecc.livemodule.live.room.LiveRoomLayout;

/**
 * 直播间信息回调监听
 */
public interface DWLiveRoomListener {

    /**
     * 切换视频文档区域
     *
     * @param state 视频是否为主区域
     */
    void onSwitchVideoDoc(LiveRoomLayout.State state);

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

    /**
     * 信息，一般包括被禁言、禁止提问等
     *
     * @param msg
     */
    void onInformation(String msg);
}
