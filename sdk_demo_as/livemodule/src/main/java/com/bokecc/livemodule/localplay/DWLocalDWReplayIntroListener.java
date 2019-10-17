package com.bokecc.livemodule.localplay;

import com.bokecc.sdk.mobile.live.pojo.RoomInfo;

/**
 * 回放直播间简介监听
 */
public interface DWLocalDWReplayIntroListener {

    /**
     * 直播间信息更新
     */
    void updateRoomInfo(RoomInfo info);
}
