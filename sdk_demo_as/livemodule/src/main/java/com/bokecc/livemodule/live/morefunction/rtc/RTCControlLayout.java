package com.bokecc.livemodule.live.morefunction.rtc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveRTCStatusListener;
import com.bokecc.livemodule.view.BaseLinearLayout;
import com.bokecc.sdk.mobile.live.DWLive;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 连麦界面
 */
public class RTCControlLayout extends BaseLinearLayout implements DWLiveRTCStatusListener {

    private LinearLayout mRTCChooseLayout;
    private LinearLayout mVideoRTCChoose;
    private LinearLayout mAudioRTCChoose;

    private LinearLayout mRTCApplyLayout;
    private TextView mRTCApplyDesc;
    private TextView mCancelRTCApply;

    private LinearLayout mRTCing;
    private TextView mRTCTime;
    private TextView mHangUpRTC;

    public RTCControlLayout(Context context) {
        super(context);
    }

    public RTCControlLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RTCControlLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.live_portrait_rtc_control, this, true);

        mRTCChooseLayout = findViewById(R.id.rtc_choose_layout);
        mVideoRTCChoose = findViewById(R.id.video_rtc_choose);
        mAudioRTCChoose = findViewById(R.id.audio_rtc_choose);

        mRTCApplyLayout = findViewById(R.id.rtc_applying);
        mRTCApplyDesc = findViewById(R.id.rtc_applying_desc);
        mCancelRTCApply = findViewById(R.id.cancel_rtc_apply);

        mRTCing = findViewById(R.id.rtc_ing);
        mRTCTime = findViewById(R.id.rtc_ing_time);
        mHangUpRTC = findViewById(R.id.hung_up_rtc);

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setDwLiveRTCStatusListener(this);
        }

        mVideoRTCChoose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()) {
                    toastOnUiThread("没有网络，请检查");
                    return;
                }
                // 调用SDK的申请视频连麦逻辑
                DWLiveCoreHandler.getInstance().startRTCConnect(true);
                mRTCChooseLayout.setVisibility(GONE);
                mRTCApplyDesc.setText("视频连麦申请中...");
                mRTCApplyLayout.setVisibility(VISIBLE);
            }
        });

        mAudioRTCChoose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()) {
                    toastOnUiThread("没有网络，请检查");
                    return;
                }
                // 调用SDK的申请音频连麦逻辑
                DWLiveCoreHandler.getInstance().startRTCConnect(false);
                mRTCChooseLayout.setVisibility(GONE);
                mRTCApplyDesc.setText("音频连麦申请中...");
                mRTCApplyLayout.setVisibility(VISIBLE);
            }
        });

        mCancelRTCApply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消连麦申请
                DWLiveCoreHandler.getInstance().cancelRTCConnect();
                mRTCChooseLayout.setVisibility(VISIBLE);
                mRTCApplyLayout.setVisibility(GONE);
                setVisibility(GONE);
            }
        });

        mHangUpRTC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toastOnUiThread("挂断连麦");
                DWLive.getInstance().disConnectSpeak();
            }
        });
    }


    //***************************************** 连麦状态监听 ***************************************

    boolean isVideoRtc = false;

    /**
     * 通知进入连麦状态
     */
    @Override
    public void onEnterRTC(boolean isVideoRtc) {
        this.isVideoRtc = isVideoRtc;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRTCChooseLayout.setVisibility(GONE);
                mRTCApplyLayout.setVisibility(GONE);
                mRTCing.setVisibility(VISIBLE);
                startCmTimer();
            }
        });
    }

    /**
     * 通知退出连麦状态
     */
    @Override
    public void onExitRTC() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRTCChooseLayout.setVisibility(VISIBLE);
                mRTCApplyLayout.setVisibility(GONE);
                mRTCing.setVisibility(GONE);
                stopCmTimer();
            }
        });
    }

    @Override
    public void onCloseSpeak() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRTCChooseLayout.setVisibility(VISIBLE);
                mRTCApplyLayout.setVisibility(GONE);
                mRTCing.setVisibility(GONE);
                setVisibility(GONE);
            }
        });
    }

    // -------------------------------- 连麦定时器相关----------------------------

    private Timer cmTimer;
    private TimerTask cmTimerTask;

    // 增加一个间隔为1s的定时器，如果断网，则增加一个10s的延时器，超过10s，重置dwlive
    private void startCmTimer() {
        cmCount = 0;

        if (cmTimer == null) {
            cmTimer = new Timer();
        }

        if (cmTimerTask != null) {
            cmTimerTask.cancel();
        }

        cmTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRTCTime.setText(formatTime(cmCount++));
                        if (!isNetworkConnected()) {
                            start10sTimerTask();
                        } else {
                            cancel10sTimerTask();
                        }
                    }
                });
            }
        };
        cmTimer.schedule(cmTimerTask, 0, 1000);
    }

    private int cmCount;

    private void stopCmTimer() {
        if (cmTimerTask != null) {
            cmTimerTask.cancel();
        }
    }

    private TimerTask cm10sTimerTask;

    private void start10sTimerTask() {
        if (cm10sTimerTask != null) {
            return;
        }

        cm10sTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DWLive.getInstance().disConnectSpeak();
                        stopCmTimer();
                        cancel10sTimerTask();
                    }
                });
            }
        };

        cmTimer.schedule(cm10sTimerTask, 10 * 1000);
    }

    private void cancel10sTimerTask() {
        if (cm10sTimerTask != null) {
            cm10sTimerTask.cancel();
            cm10sTimerTask = null;
        }

    }

    //***************************************** 工具方法 *****************************************

    private String formatTime(int t) {
        StringBuilder sb = new StringBuilder(isVideoRtc ? "视频连麦中: " : "音频连麦中: ");

        int minTime = t / 60;
        int secondTime = t % 60;

        sb.append(addZero(minTime));
        sb.append(":");
        sb.append(addZero(secondTime));
        return sb.toString();
    }

    private String addZero(int t){
        return t > 9?String.valueOf(t): String.valueOf("0" + t);
    }

}
