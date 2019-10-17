package com.bokecc.livemodule.replay.room;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.DWReplayRoomListener;
import com.bokecc.livemodule.replay.video.ReplayVideoView;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * 回放直播间信息组件
 */
public class ReplayRoomLayout extends RelativeLayout implements DWReplayRoomListener {

    private static final String TAG = "ReplayRoomLayout";

    private final int SCALE_CENTER_INSIDE = 0;
    private final int SCALE_FIT_XY = 1;
    private final int SCALE_CROP_CENTER = 2;
    private int mCurrentScaleType = SCALE_CENTER_INSIDE;

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



    TextView mDocScaleTypeView;
    private int currentType = 0;

    private SeekListener mChatListener;

    private LinearLayout mTipsLayout;

    private TextView mTipsView;


    /**
     * 播放错误，重试布局
     */
    private TextView mTryBtn;

    /**
     * 视频控制器是否应该响应手指事件
     * true:响应
     */
    private boolean controllerShouldResponseFinger;


    Timer timer = new Timer();

    TimerTask timerTask;

    private ReplayVideoView mVideoView;
    private View mCenterInside;
    private View mFitXY;
    private View mCroperCenter;

    public void setVideoView(ReplayVideoView videoView) {
        mVideoView = videoView;
    }


    public void setSeekListener(SeekListener listener) {
        mChatListener = listener;
    }

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

        mTipsLayout = findViewById(R.id.id_error_layout);
        mTryBtn = findViewById(R.id.id_try);
        mTipsView = findViewById(R.id.id_msg_tips);

//
//        mCenterInside = findViewById(R.id.id_center_inside);
//        mFitXY = findViewById(R.id.id_fit_xy);
//        mCroperCenter = findViewById(R.id.id_crop_center);



//
//        mCenterInside.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (replayRoomStatusListener != null) {
//                    replayRoomStatusListener.onClickDocScaleType(SCALE_CENTER_INSIDE);
//                }
//            }
//        });
//
//        mFitXY.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (replayRoomStatusListener != null) {
//                    replayRoomStatusListener.onClickDocScaleType(SCALE_FIT_XY);
//                }
//            }
//        });
//
//        mCroperCenter.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (replayRoomStatusListener != null) {
//                    replayRoomStatusListener.onClickDocScaleType(SCALE_CROP_CENTER);
//                }
//            }
//        });




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

        setOnClickListener(mRoomAnimatorListener);

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
            int start;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mCurrentTime.setText(TimeUtil.getFormatTime(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                start = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
                // 判断是否为空
                if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
                    return;
                }

                if (mVideoView != null) {
                    mVideoView.showProgress();
                }
                // 获取当前的player，执行seek操作
                DWReplayPlayer player = replayCoreHandler.getPlayer();
                player.seekTo(seekBar.getProgress());

                if (mChatListener != null && seekBar.getProgress() - start < 0) {
                    mChatListener.onBackSeek(seekBar.getProgress());
                }
            }
        });

        mTryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerShouldResponseFinger = true;
                mTipsLayout.setVisibility(GONE);
                if (mVideoView != null) {
                    mVideoView.showProgress();
                }
                DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
                if (instance != null) {
                    instance.start(null);
                }
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

        // 修改播放状态
        if (mPlayIcon.isSelected()) {
            mPlayIcon.setSelected(false);
            ELog.d(TAG, "changePlayerStatus# player.pause()");
            replayCoreHandler.pause();
        } else {
            mPlayIcon.setSelected(true);
            ELog.d(TAG, "changePlayerStatus# player.start()");
            replayCoreHandler.start(null);
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


    public void setCurrentTime(final long time) {
        mPlaySeekBar.post(new Runnable() {
            @Override
            public void run() {
                long playSecond = Math.round((double) time / 1000) * 1000;
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
     * 更新缓冲进度
     *
     * @param percent 缓冲百分比
     */
    @Override
    public void updateBufferPercent(final int percent) {
        Log.i(TAG, "updateBufferPercent: "+percent);
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


    /**
     * 回放播放初始化已经完成
     */
    @Override
    public void videoPrepared() {
        startTimerTask();
        IjkTimedText timedText;

    }

    @Override
    public void bufferStart() {
        stopTimerTask();
        mTopLayout.setVisibility(INVISIBLE);
        mBottomLayout.setVisibility(INVISIBLE);
    }

    @Override
    public void bufferEnd() {
        controllerShouldResponseFinger = true;
        mTopLayout.setVisibility(VISIBLE);
        mBottomLayout.setVisibility(VISIBLE);
        mTipsLayout.setVisibility(GONE);
        startTimerTask();
    }


    @Override
    public void onPlayComplete() {
        mTopLayout.post(new Runnable() {
            @Override
            public void run() {
                controllerShouldResponseFinger = false;
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放结束");
                mTryBtn.setText("重新播放");
                mPlaySeekBar.setProgress(mPlaySeekBar.getMax());
                stopTimerTask();
            }
        });

    }

    @Override
    public void onPlayError(int code) {
        mTopLayout.post(new Runnable() {
            @Override
            public void run() {
                controllerShouldResponseFinger = false;
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("视频加载失败");
                mTryBtn.setText("点击重试");
                stopTimerTask();
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

        /**
         * 点击文档类型
         */
        void onClickDocScaleType(int scaleType);
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


    private synchronized void startTimerTask() {
        stopTimerTask();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
                if (replayCoreHandler == null) {
                    return;
                }
                final DWReplayPlayer player = replayCoreHandler.getPlayer();
                if (player == null) return;
                //更新播放器的播放时间
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


    public void release() {
        startTimerTask();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //***************************************** 动画相关方法 ************************************************

    private OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!controllerShouldResponseFinger) return;
            if (mTopLayout.isShown()) {
                ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", mBottomLayout.getHeight());
                ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", -1 * mTopLayout.getHeight());
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

    public interface SeekListener {
        void onBackSeek(long progress);
    }
}