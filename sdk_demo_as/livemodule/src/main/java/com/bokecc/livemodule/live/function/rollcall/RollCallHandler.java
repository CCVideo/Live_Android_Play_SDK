package com.bokecc.livemodule.live.function.rollcall;


import android.content.Context;
import android.view.View;

import com.bokecc.livemodule.live.function.rollcall.view.RollCallPopup;

/**
 * '签到' 处理机制
 *
 * '签到' 功能 ：PC推流端发起，附带倒计时时间，观看客户端接收事件后，进行'签到'。
 */
public class RollCallHandler {

    private RollCallPopup mRollCallPopup;

    /**
     * 初始化签到功能
     */
    public void initRollCall(Context context) {
        mRollCallPopup = new RollCallPopup(context);
    }

    /**
     * 开始签到（必须在UI线程调用）
     * @param duration 倒计时时间（单位秒）
     */
    public void startRollCall(View root, final int duration) {
        if (mRollCallPopup != null) {
            mRollCallPopup.show(root);
            mRollCallPopup.startRollcall(duration);
        }
    }
}