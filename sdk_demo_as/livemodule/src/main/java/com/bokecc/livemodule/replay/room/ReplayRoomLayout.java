package com.bokecc.livemodule.replay.room;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.DWReplayRoomListener;
import com.bokecc.livemodule.replay.video.ReplayVideoView;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.livemodule.view.RePlaySeekBar;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

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
    public RePlaySeekBar mPlaySeekBar;
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

    public LinearLayout mTipsLayout;

    private TextView mTipsView;
    private TextView mSeekTime;
    private TextView mSumTime;
    private RelativeLayout mSeekRoot;

    /**
     * 播放错误，重试布局
     */
    private TextView mTryBtn;

    /**
     * 视频控制器是否应该响应手指事件
     * true:响应
     */
    public boolean controllerShouldResponseFinger = true;

    public int docMode = 1;
    Timer timer = new Timer();

    TimerTask timerTask;

    private ReplayVideoView mVideoView;
    //针对隐藏标题栏和聊天布局的延迟
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELAY_HIDE_WHAT://3s延迟隐藏
                    hide();
                    break;
            }
        }
    };
    private final int DELAY_HIDE_WHAT = 1;
    public LiveRoomLayout.State viewState = LiveRoomLayout.State.VIDEO;

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


        mDocScaleTypeView = findViewById(R.id.doc_scale_type);
        mSeekRoot = findViewById(R.id.seek_root);
        mSeekTime = findViewById(R.id.tv_seek_time);
        mSumTime = findViewById(R.id.tv_sum_time);
        mDocScaleTypeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (replayRoomStatusListener != null) {
                    if (currentType == 0) {
                        currentType = 1;
                    } else if (currentType == 1) {
                        currentType = 2;
                    } else if (currentType == 2) {
                        currentType = 0;
                    }
                    replayRoomStatusListener.onClickDocScaleType(currentType);
                }
            }
        });
        mPlaySeekBar.setCanSeek(false);
        // 设置直播间标题
        if (DWLiveReplay.getInstance().getRoomInfo() != null) {
            if (DWLiveReplay.getInstance().getRoomInfo().getBaseRecordInfo() != null && !TextUtils.isEmpty(DWLiveReplay.getInstance().getRoomInfo().getBaseRecordInfo().getTitle()))
                mTitle.setText(DWLiveReplay.getInstance().getRoomInfo().getBaseRecordInfo().getTitle());
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
                    if (viewState == LiveRoomLayout.State.VIDEO) {
                        viewState = LiveRoomLayout.State.DOC;
                        replayRoomStatusListener.switchVideoDoc(viewState);
                    } else if (viewState == LiveRoomLayout.State.DOC) {
                        viewState = LiveRoomLayout.State.VIDEO;
                        replayRoomStatusListener.switchVideoDoc(viewState);
                    } else if (viewState == LiveRoomLayout.State.OPEN_DOC) {
                        replayRoomStatusListener.switchVideoDoc(viewState);
                        viewState = LiveRoomLayout.State.VIDEO;
                    } else if (viewState == LiveRoomLayout.State.OPEN_VIDEO) {
                        replayRoomStatusListener.switchVideoDoc(viewState);
                        viewState = LiveRoomLayout.State.DOC;
                    }
                    setVideoDocSwitchText(viewState);
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
                handler.removeMessages(DELAY_HIDE_WHAT);
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
                player.seekTo(seekBar.getProgress());

                if (mChatListener != null && seekBar.getProgress() - start < 0) {
                    mChatListener.onBackSeek(seekBar.getProgress());
                }
                handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);
            }
        });

        mTryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerShouldResponseFinger = true;
                mTipsLayout.setVisibility(GONE);
                doRetry(true);
            }
        });
        handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);
        docMode = DWLiveReplay.getInstance().getRoomInfo().getDocumentDisplayMode();
    }

    public void doRetry(boolean updateStream) {
        if (mVideoView != null) {
            mVideoView.showProgress();
        }
        DWReplayCoreHandler instance = DWReplayCoreHandler.getInstance();
        if (instance != null) {
            int progress = mPlaySeekBar.getProgress();
            instance.retryReplay(progress, updateStream);
        }
    }


    // 播放/暂停
    public void changePlayerStatus() {
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        // 判断是否为空
        if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
            return;
        }
        // 修改播放状态
        if (mPlayIcon.isSelected()) {
            mPlayIcon.setSelected(false);
            replayCoreHandler.pause();
        } else {
            mPlayIcon.setSelected(true);
            replayCoreHandler.start();
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
    public void setVideoDocSwitchText(LiveRoomLayout.State state) {
        this.viewState = state;
        if (viewState == LiveRoomLayout.State.VIDEO) {
            mVideoDocSwitch.setText("切换文档");
        } else if (viewState == LiveRoomLayout.State.DOC) {
            mVideoDocSwitch.setText("切换视频");
        } else if (viewState == LiveRoomLayout.State.OPEN_DOC) {
            mVideoDocSwitch.setText("打开文档");
        } else if (viewState == LiveRoomLayout.State.OPEN_VIDEO) {
            mVideoDocSwitch.setText("打开视频");
        }
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
        if (!DWLiveReplay.getInstance().isPlayVideo()) {
            retryTime = 0;
            mPlaySeekBar.setCanSeek(true);
            startTimerTask();
            isComplete = false;
        }
    }

    @Override
    public void startRending() {
        retryTime = 0;
        mPlaySeekBar.setCanSeek(true);
        startTimerTask();
        isComplete = false;
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
        if (!isComplete) {
            mTipsLayout.setVisibility(GONE);
            startTimerTask();
        }
    }

    private boolean isComplete = false;

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
                isComplete = true;
                DWReplayCoreHandler.getInstance().getPlayer().seekTo(0);
                mPlaySeekBar.setProgress(0);
                //将倍速初始化
                DWLiveReplay.getInstance().setSpeed(1.0f);
                mReplaySpeed.setText("1.0x");
                stopTimerTask();
            }
        });
    }


    private int retryTime = 0;

    @Override
    public void onPlayError(int code) {
        stopTimerTask();
        if (retryTime < 3) {
            retryTime++;
            doRetry(false);
            return;
        }

        mTopLayout.post(new Runnable() {
            @Override
            public void run() {
                controllerShouldResponseFinger = false;
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放失败");
                mTryBtn.setText("点击重试");
                //将倍速初始化
                DWLiveReplay.getInstance().setSpeed(1.0f);
                mReplaySpeed.setText("1.0x");
            }
        });
    }

    @Override
    public void onException(final DWLiveException exception) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, exception.getMessage(), Toast.LENGTH_SHORT).show();
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
        void switchVideoDoc(LiveRoomLayout.State state);

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

        /**
         * 滑动拖拽进度
         *
         * @param max
         * @param progress
         * @param move
         * @param isSeek
         * @param xVelocity
         */
        void seek(int max, int progress, float move, boolean isSeek, float xVelocity);
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


    private void startTimerTask() {
        stopTimerTask();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                setTimeText();
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    public void setTimeText() {
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

    // 停止计时器（进度条、播放时间）
    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }


    public void release() {
        stopTimerTask();
    }

    //***************************************** 动画相关方法 ************************************************

    private OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!controllerShouldResponseFinger) return;
            handler.removeMessages(DELAY_HIDE_WHAT);
            toggleTopAndButtom();
        }
    };

    private void hide() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
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
    }

    private void show() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
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
        handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);
    }

    private void toggleTopAndButtom() {
        if (mTopLayout.isShown()) {
            hide();
        } else {
            show();
        }
    }

    public interface SeekListener {
        void onBackSeek(long progress);
    }

    private float downX, downY, moveX, moveY, downTime, upTime, seekTime;
    private VelocityTracker mVelocityTracker = null;
    private boolean isSeek = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (docMode == 1
                && mTipsLayout.getVisibility() != VISIBLE
                && mPlaySeekBar.isCanSeek()) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    moveX = event.getX();
                    downY = event.getY();
                    moveY = event.getY();
                    downTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY = event.getY();
                    if (Math.abs(downY - moveY) + 10 < Math.abs(moveX - this.downX)) {
                        if (Math.abs(moveX - this.downX) > 10 && DWReplayCoreHandler.getInstance().getPlayer().getPlayerState() != DWReplayPlayer.State.ERROR
                                && DWReplayCoreHandler.getInstance().getPlayer().getPlayerState() != DWReplayPlayer.State.BUFFERING
                                && DWReplayCoreHandler.getInstance().getPlayer().getPlayerState() != DWReplayPlayer.State.PLAYBACK_COMPLETED) {
                            stopTimerTask();
                            mVelocityTracker.computeCurrentVelocity(1000);
                            replayRoomStatusListener.seek(mPlaySeekBar.getMax(), mPlaySeekBar.getProgress(), (moveX - this.moveX) * 1000, false, mVelocityTracker.getXVelocity(0));
                            if (mSeekRoot.getVisibility() != VISIBLE)
                                mSeekRoot.setVisibility(VISIBLE);
                            mSeekTime.setText(TimeUtil.getFormatTime(mPlaySeekBar.getProgress()));
                            if (TextUtils.isEmpty(mSumTime.getText()) || mSumTime.getText().equals("00:00")) {
                                mSumTime.setText(TimeUtil.getFormatTime(mPlaySeekBar.getMax()));
                            }
                            this.moveX = moveX;
                            this.moveY = moveY;
                            //手势拖拽显示进度条
                            if (mTopLayout.getVisibility() != VISIBLE) {
                                show();
                            }
                            handler.removeMessages(DELAY_HIDE_WHAT);
                            isSeek = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float upX = event.getX();
                    float upY = event.getY();
                    upTime = System.currentTimeMillis();
                    if (isSeek) {
                        if (Math.abs(upX - downX) > 10) {
                            stopTimerTask();
                            replayRoomStatusListener.seek(mPlaySeekBar.getMax(), mPlaySeekBar.getProgress(), (upX - downX), true, mVelocityTracker.getXVelocity(0));
                        }
                        handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);
                        isSeek = false;
                        if (mSeekRoot.getVisibility() != GONE)
                            mSeekRoot.setVisibility(GONE);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (mSeekRoot.getVisibility() != GONE)
                        mSeekRoot.setVisibility(GONE);
                    break;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    performClick();
                    return false;

            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}