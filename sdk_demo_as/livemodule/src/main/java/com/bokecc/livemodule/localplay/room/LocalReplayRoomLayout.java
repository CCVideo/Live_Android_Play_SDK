package com.bokecc.livemodule.localplay.room;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.localplay.DWLocalReplayRoomListener;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.livemodule.view.RePlaySeekBar;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.util.SPUtil;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 回放直播间信息组件
 */
public class LocalReplayRoomLayout extends RelativeLayout implements DWLocalReplayRoomListener {

    private final static String TAG = LocalReplayRoomLayout.class.getSimpleName();

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
    public LinearLayout mTipsLayout;
    private TextView mTipsView;
    /**
     * 播放错误，重试布局
     */
    private TextView mTryBtn;
    public LiveRoomLayout.State viewState = LiveRoomLayout.State.VIDEO;
    private VelocityTracker mVelocityTracker;
    private RelativeLayout mSeekRoot;
    private TextView mSeekTime, mSumTime;
    public int docMode = -1;
    private boolean controllerShouldResponseFinger = true;
    private boolean isTouch;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELAY_HIDE_JUMP://3s延迟隐藏跳转view
                    llJump.setVisibility(GONE);
                    lastPosition = -1;
                    break;
            }
        }
    };
    public static final int DELAY_HIDE_JUMP = 1;

    public LocalReplayRoomLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
        initRoomListener();
    }

    public LocalReplayRoomLayout(Context context, @Nullable AttributeSet attrs) {
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
        mSeekRoot = findViewById(R.id.seek_root);
        mSeekTime = findViewById(R.id.tv_seek_time);
        mSumTime = findViewById(R.id.tv_sum_time);
        mTipsLayout = findViewById(R.id.id_error_layout);
        mTryBtn = findViewById(R.id.id_try);
        mTipsView = findViewById(R.id.id_msg_tips);
        // 隐藏 "文档/视频" 切换
        // mVideoDocSwitch.setVisibility(GONE);

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
                    if (viewState == LiveRoomLayout.State.VIDEO) {
                        viewState = LiveRoomLayout.State.DOC;
                        replayRoomStatusListener.switchVideoDoc(viewState);
                        showScaleType();
                    } else if (viewState == LiveRoomLayout.State.DOC) {
                        viewState = LiveRoomLayout.State.VIDEO;
                        replayRoomStatusListener.switchVideoDoc(viewState);
                        hideScaleType();
                    } else if (viewState == LiveRoomLayout.State.OPEN_DOC) {
                        replayRoomStatusListener.switchVideoDoc(viewState);
                        viewState = LiveRoomLayout.State.VIDEO;
                        showScaleType();
                    } else if (viewState == LiveRoomLayout.State.OPEN_VIDEO) {
                        replayRoomStatusListener.switchVideoDoc(viewState);
                        viewState = LiveRoomLayout.State.DOC;
                        hideScaleType();
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
        mPlaySeekBar.setCanSeek(true);
        mPlaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                mCurrentTime.setText(TimeUtil.getFormatTime(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouch = false;
                DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
                // 判断是否为空
                if (localReplayCoreHandler == null || localReplayCoreHandler.getPlayer() == null) {
                    return;
                }
                // 获取当前的player，执行seek操作
                DWReplayPlayer player = localReplayCoreHandler.getPlayer();
                player.seekTo(progress);
                player.start();
            }
        });
        mTryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                controllerShouldResponseFinger = true;
                mTipsLayout.setVisibility(GONE);
                doRetry(false);
            }
        });
        //跳转的部分
        llJump = findViewById(R.id.ll_jump);
        tvJumpTime = findViewById(R.id.tv_lastPosition);
        tvJump = findViewById(R.id.tv_jump);
        tvJump.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是播放中 直接seek即可
                if (DWLocalReplayCoreHandler.getInstance().getPlayer().isInPlaybackState()){
                    DWReplayPlayer player = DWLocalReplayCoreHandler.getInstance().getPlayer();
                    player.seekTo(lastPosition);
                }else{
                    //如果不是播放中  为了安全起见 重新播放
                    DWReplayCoreHandler.getInstance().retryReplay(lastPosition,true);
                }
                DWLiveReplay.getInstance().setLastPosition(lastPosition);
                llJump.setVisibility(GONE);
                mPlaySeekBar.setProgress((int) lastPosition);
                lastPosition = -1;
            }
        });
        testCase();
    }

    public void doRetry(boolean isSeek) {
        DWLocalReplayCoreHandler dwLiveLocalReplay = DWLocalReplayCoreHandler.getInstance();
        if (dwLiveLocalReplay != null) {
            int progress = mPlaySeekBar.getProgress();
            dwLiveLocalReplay.retryReplay(isSeek ? progress : 0);
        }
    }

    // 播放/暂停
    public void changePlayerStatus() {

        DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();

        // 判断是否为空
        if (localReplayCoreHandler == null || localReplayCoreHandler.getPlayer() == null) {
            return;
        }

        // 获取当前的player
        DWReplayPlayer player = localReplayCoreHandler.getPlayer();


        // 修改播放状态
        if (mPlayIcon.isSelected()) {
            mPlayIcon.setSelected(false);
            player.pause();
        } else {
            mPlayIcon.setSelected(true);
            player.start();
        }
    }

    // 倍速
    public void changePlaySpeed() {
        DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        float speed = localReplayCoreHandler.getPlayer().getSpeed(0f);
        if (speed == 0.5f) {
            localReplayCoreHandler.getPlayer().setSpeed(1.0f);
            mReplaySpeed.setText("1.0x");
        } else if (speed == 1.0f) {
            localReplayCoreHandler.getPlayer().setSpeed(1.5f);
            mReplaySpeed.setText("1.5x");
        } else if (speed == 1.5f) {
            localReplayCoreHandler.getPlayer().setSpeed(0.5f);
            mReplaySpeed.setText("0.5x");
        } else {
            mReplaySpeed.setText("1.0x");
            localReplayCoreHandler.getPlayer().setSpeed(1.0f);
        }
    }

    // 播放器当前时间
    public void setCurrentTime(final long time) {
        mPlaySeekBar.post(new Runnable() {
            @Override
            public void run() {
                long playSecond = Math.round((double) time / 1000) * 1000;
                if (!isTouch&&!isSeek){
                    mPlaySeekBar.setProgress((int) playSecond);
                }

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
        DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (localReplayCoreHandler == null) {
            return;
        }
        localReplayCoreHandler.setLocalDwReplayRoomListener(this);
    }

    /**
     * 更新直播间标题
     */
    @Override
    public void updateRoomTitle(final String title) {
        if (DWLiveLocalReplay.getInstance().getRoomInfo() != null) {
            docMode = DWLiveLocalReplay.getInstance().getRoomInfo().getDocumentDisplayMode();
        }
        if (mTitle != null) {
            mTitle.post(new Runnable() {
                @Override
                public void run() {
                    mTitle.setText(title);
                }
            });
        }
    }

    /**
     * 回放播放初始化已经完成
     */
    @Override
    public void videoPrepared() {
        startTimerTask();
        if (lastPosition>0){
            llJump.setVisibility(VISIBLE);
            long playSecond = Math.round((double) lastPosition / 1000) * 1000;
            tvJumpTime.setText(TimeUtil.getFormatTime(playSecond));
            handler.sendEmptyMessageDelayed(DELAY_HIDE_JUMP,3000);
        }
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

    @Override
    public void onPlayComplete() {
        controllerShouldResponseFinger = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopTimerTask();
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放结束");
                mTryBtn.setText("重新播放");
                DWLocalReplayCoreHandler.getInstance().getPlayer().seekTo(0);
                mPlaySeekBar.setProgress(0);
                //将倍速初始化
                if (DWLocalReplayCoreHandler.getInstance().getPlayer() != null)
                    DWLocalReplayCoreHandler.getInstance().getPlayer().setSpeed(1.0f);
                mReplaySpeed.setText("1.0x");
                Log.d(TAG, "onPlayComplete");
                DWLocalReplayCoreHandler.getInstance().stop();

            }
        });
    }

    @Override
    public void onPlayError(int code) {
        stopTimerTask();
        controllerShouldResponseFinger = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                mTopLayout.setVisibility(INVISIBLE);
                mBottomLayout.setVisibility(INVISIBLE);
                mTipsLayout.setVisibility(VISIBLE);
                mTipsView.setText("播放失败");
                mTryBtn.setText("点击重试");
                //将倍速初始化
                if (DWLocalReplayCoreHandler.getInstance().getPlayer() != null)
                    DWLocalReplayCoreHandler.getInstance().getPlayer().setSpeed(1.0f);
                mReplaySpeed.setText("1.0x");
                DWLocalReplayCoreHandler.getInstance().stop();
            }
        });
    }


    /****************************** 回放直播间状态监听 用于Activity更新UI ******************************/

    /**
     * 回放直播间状态监听，用于Activity更新UI
     */
    public interface LocalReplayRoomStatusListener {

        /**
         * 视频/文档 切换
         *
         * @param viewState
         */
        void switchVideoDoc(LiveRoomLayout.State viewState);

        /**
         * 退出直播间
         */
        void closeRoom();

        /**
         * 进入全屏
         */
        void fullScreen();

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

        /**
         * 点击文档类型
         */
        void onClickDocScaleType(int scaleType);
    }

    // 回放直播间状态监听
    private LocalReplayRoomStatusListener replayRoomStatusListener;

    /**
     * 设置回放直播间状态监听
     *
     * @param listener 回放直播间状态监听
     */
    public void setReplayRoomStatusListener(LocalReplayRoomStatusListener listener) {
        this.replayRoomStatusListener = listener;
    }

    /******************************* 定时任务 用于更新进度条等 UI ***************************************/

    Timer timer = new Timer();

    TimerTask timerTask;
    long timerTime = 0;
    // 开始时间任务
    private void startTimerTask() {
        stopTimerTask();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                DWLocalReplayCoreHandler localReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
                // 判断是否为空
                if (localReplayCoreHandler == null || localReplayCoreHandler.getPlayer() == null) {
                    return;
                }
                // 获取当前的player
                final DWReplayPlayer player = localReplayCoreHandler.getPlayer();
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
                if (timerTime%5==0&&localReplayCoreHandler.getPlayer()!=null){
                    com.bokecc.sdk.mobile.live.util.SPUtil.getInstance().put(DWLocalReplayCoreHandler.LASTPOSITION, DWLocalReplayCoreHandler.getInstance().getPlayer().getCurrentPosition());
                    com.bokecc.sdk.mobile.live.util.SPUtil.getInstance().put(DWLocalReplayCoreHandler.RECORDID, recordId);
                }

            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    // 停止计时器（进度条、播放时间）
    public void stopTimerTask() {
        timerTime = 0;
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
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
                show();
            }
        }
    };

    public void show() {
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

    private float downX, downY, moveX, moveY, downTime, upTime, seekTime;
    private boolean isSeek = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (docMode == 1 && mTipsLayout.getVisibility() != VISIBLE) {
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
                        if (Math.abs(moveX - this.downX) > 10 && DWLocalReplayCoreHandler.getInstance().getPlayer().isInPlaybackState()) {
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
                            isSeek = true;
                            if (!mTopLayout.isShown()) {
                                show();
                            }
                        }
                    }


                    break;
                case MotionEvent.ACTION_UP:
                    float upX = event.getX();
                    float upY = event.getY();
                    upTime = System.currentTimeMillis();
                    if (isSeek) {
                        mSeekRoot.setVisibility(GONE);
                        if (Math.abs(upX - downX) > 10) {
                            replayRoomStatusListener.seek(mPlaySeekBar.getMax(), mPlaySeekBar.getProgress(), (upX - downX), true, mVelocityTracker.getXVelocity(0));
                            isSeek = false;
                        }
                        startTimerTask();
                    }
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


    // --------------------------------------测试拉伸模式-------------------------------------------
    private boolean isShowScale = false;// 是否显示拉伸类型选项
    // 文档拉伸类型
    private Spinner spinner;

    public void testCase() {
        spinner = findViewById(R.id.spr_scale_type);
        if (!isShowScale) {
            spinner.setVisibility(View.GONE);
        } else {
            spinner.setVisibility(View.VISIBLE);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                // 如果文档是适应窗口模式
                if (replayRoomStatusListener != null) {
                    replayRoomStatusListener.onClickDocScaleType(pos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 显示拉伸类型按钮
    public void showScaleType() {
        if (isShowScale) {
            spinner.setVisibility(View.VISIBLE);
        }

    }

    // 隐藏拉伸类型按钮
    public void hideScaleType() {
        spinner.setVisibility(View.GONE);
    }

    private LinearLayout llJump;
    private TextView tvJumpTime;
    private TextView tvJump;
    private long lastPosition;
    private String recordId;
    public void showJump(long lastPosition, String recordId) {
        if (lastPosition>0)
            this.recordId=recordId;{
            this.lastPosition = lastPosition;
        }
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
}
