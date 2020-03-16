package com.bokecc.livemodule.live.chat.module;

import android.support.annotation.Nullable;

/**
 * 聊天用户
 */
public class ChatUser {

    /**
     * 用户id
     */
    private String mUserId;
    /**
     * 用户名
     */
    private String mUserName;
    /**
     * 可以为空 用户角色
     */
    private @Nullable
    String mUserRole;
    /**
     * 可以为空 用户头像地址
     */
    private @Nullable String mUserAvatar;

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getUserRole() {
        return mUserRole;
    }

    public void setUserRole(String userRole) {
        mUserRole = userRole;
    }

    public String getUserAvatar() {
        return mUserAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.mUserAvatar = userAvatar;
    }

}