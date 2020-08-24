package com.bokecc.livemodule.live.function.practice;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
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
    private boolean close = false;//是否已经关闭
    Timer timer;
    TimerTask timerTask;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    @Override
    public void onClose() {
        close = true;
    }

    public void startPractice(View root, PracticeInfo info, PracticeListener practiceListener) {
        close = false;
        //判断随堂测是否已结束
        if (info.getStatus() == 2) {
            return;
        }
        if (mPracticePopup.isShowing()&&(mPracticePopup.practiceInfo==null||!mPracticePopup.practiceInfo.getId().equals(info.getId()))){
            mPracticePopup.dismiss();
        }
        if (mPracticeLandPopup.isShowing()&&(mPracticeLandPopup.practiceInfo==null||!mPracticeLandPopup.practiceInfo.getId().equals(info.getId()))){
            mPracticeLandPopup.dismiss();
        }
        startTimer(info,0);
        if (isPortrait(mContext)) {
            mPracticePopup.startPractice(info,practiceListener);
            mPracticePopup.show(root);
        } else {
            mPracticeLandPopup.startPractice(info,practiceListener);
            mPracticeLandPopup.show(root);
        }
    }
    public void startPractice(View root, PracticeConfig practiceConfig, PracticeListener practiceListener) {
        close = false;
        close = false;
        //判断随堂测是否已结束
        if (practiceConfig.getPracticeInfo().getStatus() == 2) {
            return;
        }
        startTimer(practiceConfig.getPracticeInfo(),this.departTime);
        if (isPortrait(mContext)) {
            mPracticePopup.startPractice(practiceConfig,practiceListener);
            mPracticePopup.show(root);
        } else {
            mPracticeLandPopup.startPractice(practiceConfig,practiceListener);
            mPracticeLandPopup.show(root);
        }
    }

    // 展示随堂测提交结果
    public void showPracticeSubmitResult(View root, PracticeSubmitResultInfo info) {
        if (mSubmitResultPopup.isShowing()){
            mSubmitResultPopup.dismiss();
        }
        mSubmitResultPopup.showPracticeSubmitResult(info);
        mSubmitResultPopup.show(root);
    }
    // 展示随堂测统计数据
    public void showPracticeStatis(View root, PracticeStatisInfo info) {
        if (mPracticePopup.isShowing()){
            mPracticePopup.dismiss();
        }
        if (mPracticeLandPopup.isShowing()){
            mPracticeLandPopup.dismiss();
        }
        if (info.getStatus()==2){
            stopTimer();
        }
        if (info.getStatus()==3){
            onPracticeClose(info.getId());
            stopTimer();
        }
        if (close){
            return;
        }
        if (isPortrait(mContext)) {
            mPracticeStatisPopup.showPracticeStatis(info);
            if (!mPracticeStatisPopup.isShowing())
                mPracticeStatisPopup.show(root, this);
            if (info.getStatus()==2){
                String formatTime = TimeUtil.getFormatTime(info.getStopTime()*1000);
                mPracticeStatisPopup.setText(formatTime);
            }else{
                mPracticeStatisPopup.setText(formatTime);
            }

        } else {
            mPracticeStatisLandPopup.showPracticeStatis(info);
            if (!mPracticeStatisPopup.isShowing())
                mPracticeStatisLandPopup.show(root, this);
            if (info.getStatus()==2){
                mPracticeStatisLandPopup.setText(TimeUtil.getFormatTime(info.getStopTime()));
            }else{
                mPracticeStatisLandPopup.setText(formatTime);
            }
        }
    }
    // 停止随堂测
    public void onPracticeStop(String practiceId, View root,boolean isSmall) {
        if (mPracticePopup.isShowing()||mPracticeLandPopup.isShowing()||isSmall){//如果是未提交 请求下统计界面
            //关闭原先的弹框
            if (mPracticePopup.isShowing()){
                mPracticePopup.dismiss();
            }
            if (mPracticeLandPopup.isShowing()){
                mPracticeLandPopup.dismiss();
            }
            DWLive.getInstance().getPracticeStatis(practiceId);
            stopTimer();
            return;
        }

        if (isPortrait(mContext)) {
            if (mPracticeStatisPopup != null) {
                mPracticeStatisPopup.showPracticeStop();
                if (!mPracticeStatisPopup.isShowing()){
                    mPracticeStatisPopup.show(root);
                }
            }
        }else{
            if (mPracticeStatisLandPopup != null) {
                mPracticeStatisLandPopup.showPracticeStop();
                if (!mPracticeStatisLandPopup.isShowing()){
                    mPracticeStatisLandPopup.show(root);
                }
            }
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

    public void startTimer(final PracticeInfo practiceInfo,long departTime) {
        if (timer != null && timerTask != null) {
            timer.cancel();
            timerTask.cancel();
        }
        timer = new Timer();
        if (this.departTime<=0){
            this.departTime = System.currentTimeMillis() - practiceInfo.getServerTime();
        }else{
            this.departTime=departTime;
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    final long now = System.currentTimeMillis() - PracticeHandler.this.departTime;
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
    // 判断当前屏幕朝向是否为竖屏
    private boolean isPortrait(Context context) {
        int mOrientation = context.getResources().getConfiguration().orientation;
        return mOrientation != Configuration.ORIENTATION_LANDSCAPE;
    }

}
