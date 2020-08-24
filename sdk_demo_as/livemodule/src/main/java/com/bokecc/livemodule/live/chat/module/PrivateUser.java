package com.bokecc.livemodule.live.chat.module;

/**
 * 私聊用户
 */
public class PrivateUser {

    private String mId;
    private String mName;
    private String mAvatar;
    private String mMsg;
    private String mTime;
    private String mRole;

    private boolean isRead;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getRole() {
        return mRole;
    }

    public void setRole(String mRole) {
        this.mRole = mRole;
    }
}