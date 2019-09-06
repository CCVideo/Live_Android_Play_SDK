package com.bokecc.livemodule.live.function.practice;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import com.bokecc.livemodule.live.function.practice.view.PracticeLandPopup;
import com.bokecc.livemodule.live.function.practice.view.PracticePopup;
import com.bokecc.livemodule.live.function.practice.view.PracticeStatisLandPopup;
import com.bokecc.livemodule.live.function.practice.view.PracticeStatisPopup;
import com.bokecc.livemodule.live.function.practice.view.PracticeSubmitResultPopup;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;

/**
 *  '随堂测' 处理机制
 */
public class PracticeHandler {

    private Context mContext;
    PracticePopup mPracticePopup;  // 随堂测答题弹出界面
    PracticeLandPopup mPracticeLandPopup; // 随堂测答题弹出界面(横屏)
    PracticeSubmitResultPopup mSubmitResultPopup;  // 随堂测答题结果弹出界面
    PracticeStatisPopup mPracticeStatisPopup; // 随堂测答题统计界面
    PracticeStatisLandPopup mPracticeStatisLandPopup; // 随堂测答题统计界面(横屏)

    /**
     * 初始化随堂测功能
     */
    public void initPractice(Context context) {
        mContext = context.getApplicationContext();
        mPracticePopup = new PracticePopup(mContext);
        mPracticeLandPopup = new PracticeLandPopup(mContext);
        mSubmitResultPopup = new PracticeSubmitResultPopup(mContext);
        mPracticeStatisPopup = new PracticeStatisPopup(mContext);
        mPracticeStatisLandPopup = new PracticeStatisLandPopup(mContext);
    }

    // 开始随堂测
    public void startPractice(View root, PracticeInfo info) {
        if (isPortrait(mContext)) {
            mPracticePopup.startPractice(info);
            mPracticePopup.show(root);
        } else {
            mPracticeLandPopup.startPractice(info);
            mPracticeLandPopup.show(root);
        }
    }

    // 展示随堂测提交结果
    public void showPracticeSubmitResult(View root, PracticeSubmitResultInfo info) {
        mSubmitResultPopup.showPracticeSubmitResult(info);
        mSubmitResultPopup.show(root);
    }

    // 展示随堂测统计数据
    public void showPracticeStatis(View root, PracticeStatisInfo info) {
        if (isPortrait(mContext)) {
            mPracticeStatisPopup.showPracticeStatis(info);
            mPracticeStatisPopup.show(root);
        } else {
            mPracticeStatisLandPopup.showPracticeStatis(info);
            mPracticeStatisLandPopup.show(root);
        }
    }

    // 停止随堂测
    public void onPracticeStop(String practiceId) {
        if (mPracticePopup != null && mPracticePopup.isShowing()) {
            mPracticePopup.dismiss();
        }

        if (mPracticeLandPopup != null && mPracticeLandPopup.isShowing()) {
            mPracticeLandPopup.dismiss();
        }

        if (mPracticeStatisPopup != null && mPracticeStatisPopup.isShowing()) {
            mPracticeStatisPopup.showPracticeStop();
        }

        if (mPracticeStatisLandPopup != null && mPracticeStatisLandPopup.isShowing()) {
            mPracticeStatisLandPopup.showPracticeStop();
        }
    }

    // 关闭随堂测
    public void onPracticeClose(String practiceId) {
        //  关闭所有和随堂测有关的弹窗

        if (mPracticePopup != null && mPracticePopup.isShowing()) {
            mPracticePopup.dismiss();
        }

        if (mPracticeLandPopup != null && mPracticeLandPopup.isShowing()) {
            mPracticeLandPopup.dismiss();
        }

        if (mSubmitResultPopup != null && mSubmitResultPopup.isShowing()) {
            mSubmitResultPopup.dismiss();
        }

        if (mPracticeStatisPopup != null && mPracticeStatisPopup.isShowing()) {
            mPracticeStatisPopup.dismiss();
        }

        if (mPracticeStatisLandPopup != null && mPracticeStatisLandPopup.isShowing()) {
            mPracticeStatisLandPopup.dismiss();
        }
    }

    // 判断当前屏幕朝向是否为竖屏
    private boolean isPortrait(Context context) {
        int mOrientation = context.getResources().getConfiguration().orientation;
        return mOrientation != Configuration.ORIENTATION_LANDSCAPE;
    }
}
