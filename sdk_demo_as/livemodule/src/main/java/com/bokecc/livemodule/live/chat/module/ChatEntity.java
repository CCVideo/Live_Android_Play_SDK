package com.bokecc.livemodule.live.chat.module;

/**
 * 聊天实例类
 */
public class ChatEntity {
    /**
     * 此聊天对应的ID
     */
    private String mChatId;
    /**
     * 发送该条信息的用户id
     */
    private String mUserId;
    /**
     * 发送该条信息的用户名
     */
    private String mUserName;
    /**
     * 发送该条信息的用户头像
     */
    private String mUserAvatar;
    /**
     * 发送该条信息的角色
     */
    private String mUserRole;
    /**
     * 该条信息是否是私聊
     */
    private boolean isPrivate;
    /**
     * 该条信息是私聊的情况下 是否是主播发送的
     */
    private boolean isPublisher;
    /**
     *该条信息是私聊的情况下 接收方的id
     */
    private String mReceiveUserId;
    /**
     *该条信息是私聊的情况下 接收方的用户名
     */
    private String mReceivedUserName;
    /**
     * 该条信息是私聊的情况下 接收方的头像地址
     */
    private String mReceiveUserAvatar;
    /**
     * 聊天内容
     */
    private String mMsg;
    /**
     * 聊天时间
     */
    private String mTime;
    /**
     * 聊天状态（聊天消息的状态 0：显示 1：不显示）
     */
    private String mStatus;

    public String getChatId() {
        return mChatId;
    }

    public void setChatId(String chatId) {
        this.mChatId = chatId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserAvatar() {
        return mUserAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        mUserAvatar = userAvatar;
    }

    public String getUserRole() {
        return mUserRole;
    }

    public void setUserRole(String mUserRole) {
        this.mUserRole = mUserRole;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isPublisher() {
        return isPublisher;
    }

    public void setPublisher(boolean publisher) {
        isPublisher = publisher;
    }

    public String getReceiveUserId() {
        return mReceiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        mReceiveUserId = receiveUserId;
    }

    public String getReceivedUserName() {
        return mReceivedUserName;
    }

    public void setReceivedUserName(String receivedUserName) {
        mReceivedUserName = receivedUserName;
    }

    public String getReceiveUserAvatar() {
        return mReceiveUserAvatar;
    }

    public void setReceiveUserAvatar(String receiveUserAvatar) {
        mReceiveUserAvatar = receiveUserAvatar;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }
}

