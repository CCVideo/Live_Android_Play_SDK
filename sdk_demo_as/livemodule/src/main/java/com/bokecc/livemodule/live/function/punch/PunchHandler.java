package com.bokecc.livemodule.live.function.punch;

import android.content.Context;
import android.view.View;

import com.bokecc.livemodule.live.function.punch.view.PunchDialog;
import com.bokecc.sdk.mobile.live.pojo.PunchAction;

/**
 * '投票' 处理机制
 */
public class PunchHandler {

    private PunchDialog mPunchPopup;

    private boolean isVoteResultShow = false;
    private Context context;
    /**
     * 初始化投票功能
     */
    public void initPunch(Context context) {
       this.context=context;
    }

    /** 显示签到 */
    public void startPunch(View root, PunchAction punchAction) {
        if (mPunchPopup!=null&&mPunchPopup.isShowing()){
            mPunchPopup.dismiss();
        }
        if (mPunchPopup!=null){
            mPunchPopup = null;
        }
        mPunchPopup = new PunchDialog(context);
        int time = punchAction.getRemainDuration() - 2;
        if (time <= 0) {
            return;
        }
        mPunchPopup.setPunchId(punchAction.getId());
        mPunchPopup.setTime(time);
        mPunchPopup.show(root);
    }

    /** 结束签到
     * @param message*/
    public void stopPunch(String message) {
        if (mPunchPopup !=null&& mPunchPopup.isShowing()) {
            mPunchPopup.destroy(message);
        }
    }
}
