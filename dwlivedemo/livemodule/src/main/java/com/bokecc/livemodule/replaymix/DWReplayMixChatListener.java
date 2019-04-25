package com.bokecc.livemodule.replaymix;

import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;

import java.util.TreeSet;

/**
 * 回放聊天监听
 */
public interface DWReplayMixChatListener {

    /**
     * 回调回放聊天信息
     *
     * @param replayChatMsgs 回放聊天信息
     */
    void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs);
}
