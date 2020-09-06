package com.bokecc.livemodule.live;

import com.bokecc.sdk.mobile.live.pojo.UserRedminAction;

public interface UserListener {
    /**
     *  用户进出通知
     * @param userJoinExitAction
     *    userId 用户id
     *    userName 用户名
     *    userRole 用户角色
     *    userAvatar 用户头像
     *    groupId 分组id
     *    role 接收端列表  1-讲师；2-助教；3-主持人；4-观看端
     *    content 自定义内容
     *    type 类型
     *        {@link UserRedminAction.ActionType#HDUSER_IN_REMIND} 进入直播间
     *        {@link UserRedminAction.ActionType#HDUSER_OUT_REMIND}   退出直播间
     */
    void HDUserRemindWithAction( UserRedminAction userJoinExitAction);
}
