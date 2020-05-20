package com.bokecc.livemodule.live.function.practice;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import com.bokecc.livemodule.live.function.practice.view.OnCloseListener;
import com.bokecc.livemodule.live.function.practice.view.PracticeLandPopup;
import com.bokecc.livemodule.live.function.practice.view.PracticePopup;
import com.bokecc.livemodule.live.function.practice.view.PracticeStatisLandPopup;
import com.bokecc.livemodule.live.function.practice.view.PracticeStatisPopup;
import com.bokecc.livemodule.live.function.practice.view.PracticeSubmitResultPopup;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * '随堂测' 处理机制
 */
public class PracticeHandler implements OnCloseListener {

    private Context mContext;
    PracticePopup mPracticePopup;  // 随堂测答题弹出界面
    PracticeLandPopup mPracticeLandPopup; // 随堂测答题弹出界面(横屏)
    PracticeSubmitResultPopup mSubmitResultPopup;  // 随堂测答题结果弹出界面
    PracticeStatisPopup mPracticeStatisPopup; // 随堂测答题统计界面
    PracticeStatisLandPopup mPracticeStatisLandPopup; // 随堂测答题统计界面(横屏)
    private long departTime;
    private String formatTime;

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
        //判断随堂测是否已结束
        if (info.getStatus() == 2) {
            return;
        }
        startTimer(info);
        mPracticeStatisLandPopup.isClose.set(false);
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
            if (!mPracticeStatisPopup.isShowing()) {
                mPracticeStatisPopup.show(root, this);
                mPracticeStatisPopup.setText(formatTime);
            }
        } else {
            if (!mPracticeStatisLandPopup.isClose.get()){
                mPracticeStatisLandPopup.showPracticeStatis(info);
                if (!mPracticeStatisLandPopup.isShowing()) {
                    mPracticeStatisLandPopup.show(root, this);
                    mPracticeStatisLandPopup.setText(formatTime);
                }
            }
        }
    }

    @Override
    public void onClose() {
        stopTimer();
    }

    // 停止随堂测
    public void onPracticeStop(String practiceId) {
        if (mPracticePopup != null && mPracticePopup.isShowing()) {
            mPracticePopup.dismiss();
            //如果还没有提交问题 需要去手动获取结果
            DWLive.getInstance().getPracticeStatis(practiceId);
        }

        if (mPracticeLandPopup != null && mPracticeLandPopup.isShowing()) {
            mPracticeLandPopup.dismiss();
            //如果还没有提交问题 需要去手动获取结果
            DWLive.getInstance().getPracticeStatis(practiceId);
        }

        if (mPracticeStatisPopup != null && mPracticeStatisPopup.isShowing()) {
            mPracticeStatisPopup.showPracticeStop();

        }

        if (mPracticeStatisLandPopup != null && mPracticeStatisLandPopup.isShowing()) {
            mPracticeStatisLandPopup.showPracticeStop();

        }

        stopTimer();
    }

    // 关闭随堂测
    public void onPracticeClose(String practiceId) {
        stopTimer();
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

    Timer timer;
    TimerTask timerTask;

    public void startTimer(final PracticeInfo practiceInfo) {
        if (timer != null && timerTask != null) {
            return;
        }
        timer = new Timer();
        departTime = System.currentTimeMillis() - practiceInfo.getServerTime();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    final long now = System.currentTimeMillis() - departTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final Date date = sdf.parse(practiceInfo.getPublishTime());
                    formatTime = TimeUtil.getFormatTime(now - date.getTime());
                    if (mPracticePopup != null && mPracticePopup.isShowing()) {
                        mPracticePopup.setText(formatTime);
                    }

                    if (mPracticeLandPopup != null && mPracticeLandPopup.isShowing()) {
                        mPracticeLandPopup.setText(formatTime);
                    }

                    if (mPracticeStatisPopup != null && mPracticeStatisPopup.isShowing()) {
                        mPracticeStatisPopup.setText(formatTime);
                    }

                    if (mPracticeStatisLandPopup != null && mPracticeStatisLandPopup.isShowing()) {
                        mPracticeStatisLandPopup.setText(formatTime);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(timerTask, 0, 1000);
    }

    public void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
