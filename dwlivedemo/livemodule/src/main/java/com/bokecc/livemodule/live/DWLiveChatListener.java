package com.bokecc.livemodule.live;

import com.bokecc.sdk.mobile.live.pojo.ChatMessage;

import java.util.ArrayList;


/**
 * 直播聊天回调监听
 */
public interface DWLiveChatListener {

    /**
     * 收到直播历史公聊
     *
     * @param chatLogs 补推的历史聊天信息
     */
    void onHistoryChatMessage(ArrayList<ChatMessage> chatLogs);

    /**
     * 公共聊天
     *
     * @param msg 聊天信息
     */
    void onPublicChatMessage(ChatMessage msg);

    /**
     * 收到聊天信息状态管理事件
     *
     * @param msgStatusJson 聊天信息状态管理事件json
     */
    void onChatMessageStatus(String msgStatusJson);

    /**
     * 禁言消息，该消息是单个用户被禁言情况下发送消息的回调
     *
     * @param msg 聊天信息
     */
    void onSilenceUserChatMessage(ChatMessage msg);

    /**
     * 收到禁言事件
     *
     * @param mode 禁言类型 1：个人禁言  2：全员禁言
     */
    void onBanChat(int mode);

    /**
     * 收到解除禁言事件
     *
     * @param mode 禁言类型 1：个人禁言  2：全员禁言
     */
    void onUnBanChat(int mode);


    /**
     * 收到广播信息
     */
    void onBroadcastMsg(String msg);


}
