package com.bokecc.livemodule.replay.room;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.DWReplayRoomListener;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 回放直播间信息组件
 */
public class ReplayRoomLayout extends RelativeLayout implements DWReplayRoomListener {

    private static final String TAG = "ReplayRoomLayout";

    Context mContext;

    RelativeLayout mTopLayout;
    RelativeLayout mBottomLayout;

    TextView mTitle;
    TextView mVideoDocSwitch;
    ImageView mClose;

    // 当前播放时间
    TextView mCurrentTime;
    // 进度条
    SeekBar mPlaySeekBar;
    // 播放时长
    TextView mDurationView;
    // 播放/暂停 按钮
    ImageView mPlayIcon;
    // 倍速按钮
    Button mReplaySpeed;
    // 全屏按钮
    ImageView mLiveFullScreen;

    public ReplayRoomLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
        initRoomListener();
    }

    public ReplayRoomLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
        initRoomListener();
    }

    private void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.replay_room_layout, this, true);
        mTitle = findViewById(R.id.tv_portrait_live_title);
        mTopLayout = findViewById(R.id.rl_portrait_live_top_layout);
        mBottomLayout = findViewById(R.id.rl_portrait_live_bottom_layout);
        mVideoDocSwitch = findViewById(R.id.video_doc_switch);
        mLiveFullScreen = findViewById(R.id.iv_portrait_live_full);
        mClose = findViewById(R.id.iv_portrait_live_close);
        mReplaySpeed = findViewById(R.id.replay_speed);
        mPlayIcon = findViewById(R.id.replay_play_icon);
        mCurrentTime = findViewById(R.id.replay_current_time);
        mDurationView = findViewById(R.id.replay_duration);
        mPlaySeekBar = findViewById(R.id.replay_progressbar);
        mPlayIcon.setSelected(true);

        // 设置直播间标题
        if (DWLiveReplay.getInstance().getRoomInfo() != null) {
            mTitle.setText(DWLiveReplay.getInstance().getRoomInfo().getName());
        }

        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
            if (!dwReplayCoreHandler.hasPdfView()) {
                mVideoDocSwitch.setVisibility(GONE);
            }
        }

        this.setOnClickListener(mRoomAnimatorListener);

        mPlayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerStatus();
            }
        });

        mReplaySpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlaySpeed();
            }
        });

        mVideoDocSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.switchVideoDoc();
                }
            }
        });

        mLiveFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intoFullScreen();
            }
        });

        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.closeRoom();
                }
            }
        });

        mPlaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
                // 判断是否为空
                if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
                    return;
                }
                // 获取当前的player，执行seek操作
                DWReplayPlayer player = replayCoreHandler.getPlayer();
                player.seekTo(progress);
                player.start();
            }
        });
    }

    // 播放/暂停
    public void changePlayerStatus() {

        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        // 判断是否为空
        if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
            return;
        }

        // 获取当前的player
        DWReplayPlayer player = replayCoreHandler.getPlayer();

        // 修改播放状态
        if (mPlayIcon.isSelected()) {
            mPlayIcon.setSelected(false);
            LogUtil.d(TAG,"changePlayerStatus# player.pause()");
            player.pause();
        } else {
            mPlayIcon.setSelected(true);
            LogUtil.d(TAG,"changePlayerStatus# player.start()");
            player.start();
        }
    }

    // 倍速
    public void changePlaySpeed() {
        float speed = DWLiveReplay.getInstance().getSpeed();
        if (speed == 0.5f) {
            DWLiveReplay.getInstance().setSpeed(1.0f);
            mReplaySpeed.setText("1.0x");
        } else if (speed == 1.0f) {
            DWLiveReplay.getInstance().setSpeed(1.5f);
            mReplaySpeed.setText("1.5x");
        } else if (speed == 1.5f) {
            DWLiveReplay.getInstance().setSpeed(0.5f);
            mReplaySpeed.setText("0.5x");
        } else {
            mReplaySpeed.setText("1.0x");
            DWLiveReplay.getInstance().setSpeed(1.0f);
        }
    }

    // 播放器当前时间
    public void setCurrentTime(final long time) {
        mPlaySeekBar.post(new Runnable() {
            @Override
            public void run() {
                long playSecond = Math.round((double) time / 1000) * 1000;
                mCurrentTime.setText(TimeUtil.getFormatTime(playSecond));
                mPlaySeekBar.setProgress((int) playSecond);
            }
        });
    }

    // 设置文档/视频切换的按钮的文案
    public void setVideoDocSwitchText(String text) {
        mVideoDocSwitch.setText(text);
    }

    // 进入全屏
    public void intoFullScreen() {
        // 回调给activity修改ui
        if (replayRoomStatusListener != null) {
            replayRoomStatusListener.fullScreen();
        }
        mLiveFullScreen.setVisibility(GONE);
    }

    // 退出全屏
    public void quitFullScreen() {
        mLiveFullScreen.setVisibility(VISIBLE);
    }

    /****************************** 回放直播间监听 用于Core Handler 触发相关逻辑 ***************************/

    // 初始化回放直播间监听
    private void initRoomListener() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler == null) {
            return;
        }
        dwReplayCoreHandler.setReplayRoomListener(this);
    }

    /**
     * 回放播放初始化已经完成
     */
    @Override
    public void videoPrepared() {
        startTimerTask();
    }

    /**
     * 更新缓冲进度
     *
     * @param percent 缓冲百分比
     */
    @Override
    public void updateBufferPercent(final int percent) {
        mPlaySeekBar.post(new Runnable() {
            @Override
            public void run() {
                mPlaySeekBar.setSecondaryProgress((int) ((double) mPlaySeekBar.getMax() * percent / 100));
            }
        });
    }

    /**
     * 展示播放的视频时长
     */
    @Override
    public void showVideoDuration(final long playerDuration) {
        mPlaySeekBar.post(new Runnable() {
            @Override
            public void run() {
                long playSecond = Math.round((double) playerDuration / 1000) * 1000;
                mDurationView.setText(TimeUtil.getFormatTime(playSecond));
                mPlaySeekBar.setMax((int) playSecond);
            }
        });
    }

    /****************************** 回放直播间状态监听 用于Activity更新UI ******************************/

    /**
     * 回放直播间状态监听，用于Activity更新UI
     */
    public interface ReplayRoomStatusListener {

        /**
         * 视频/文档 切换
         */
        void switchVideoDoc();

        /**
         * 退出直播间
         */
        void closeRoom();

        /**
         * 进入全屏
         */
        void fullScreen();
    }

    // 回放直播间状态监听
    private ReplayRoomStatusListener replayRoomStatusListener;

    /**
     * 设置回放直播间状态监听
     *
     * @param listener 回放直播间状态监听
     */
    public void setReplayRoomStatusListener(ReplayRoomStatusListener listener) {
        this.replayRoomStatusListener = listener;
    }

    /******************************* 定时任务 用于更新进度条等 UI ***************************************/

    Timer timer = new Timer();

    TimerTask timerTask;
    // 开始时间任务
    private void startTimerTask() {
        stopTimerTask();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
                // 判断是否为空
                if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
                    return;
                }
                // 获取当前的player
                final DWReplayPlayer player = replayCoreHandler.getPlayer();
                if (!player.isPlaying() && (player.getDuration() - player.getCurrentPosition() < 500)) {
                    setCurrentTime(player.getDuration());
                } else {
                    setCurrentTime(player.getCurrentPosition());
                }

                mPlayIcon.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayIcon.setSelected(player.isPlaying());
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    // 停止计时器（进度条、播放时间）
    public void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    //***************************************** 动画相关方法 ************************************************

    private OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mTopLayout.isShown()) {
                ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", mBottomLayout.getHeight());
                ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", - 1 * mTopLayout.getHeight());
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(top_y).with(bottom_y);

                //播放动画的持续时间
                animatorSet.setDuration(500);
                animatorSet.start();

                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mBottomLayout.setVisibility(GONE);
                        mTopLayout.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            } else {
                mTopLayout.setVisibility(VISIBLE);
                mBottomLayout.setVisibility(VISIBLE);
                ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", 0);
                ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", 0);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(top_y).with(bottom_y);
                //播放动画的持续时间
                animatorSet.setDuration(500);
                animatorSet.start();
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
        }
    };
}