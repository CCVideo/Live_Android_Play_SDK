package com.bokecc.livemodule.live;

import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.sdk.mobile.live.pojo.PrivateChatInfo;

public interface DWLiveMoreFunctionListener {

    //--------------------------------------- 公告 ---------------------------------------------

    /**
     * 公告
     *
     * @param isRemove     是否是公告删除，如果为true，表示公告删除且announcement参数为null
     * @param announcement 公告内容
     */
    void onAnnouncement(boolean isRemove, String announcement);

    //--------------------------------------- 私聊 ---------------------------------------------

    /**
     * 跳转并展示与此聊天相关的私聊列表信息
     *
     * @param chatEntity 公聊消息
     */
    void jump2PrivateChat(ChatEntity chatEntity);

    /**
     * 别人私聊我
     *
     * @param info
     */
    void onPrivateChat(PrivateChatInfo info);

    /**
     * 我发出的私聊
     *
     * @param info
     */
    void onPrivateChatSelf(PrivateChatInfo info);



}
