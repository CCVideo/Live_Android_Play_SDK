package com.bokecc.dwlivedemo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.dwlivedemo.utils.Permissions;
import com.bokecc.livemodule.live.DWLiveBarrageListener;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveRTCListener;
import com.bokecc.livemodule.live.chat.KeyboardHeightProvider;
import com.bokecc.livemodule.live.chat.LiveChatComponent;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.live.chat.barrage.BarrageLayout;
import com.bokecc.livemodule.live.chat.util.DensityUtil;
import com.bokecc.livemodule.live.doc.LiveDocComponent;
import com.bokecc.livemodule.live.function.FunctionCallBack;
import com.bokecc.livemodule.live.function.FunctionHandler;
import com.bokecc.livemodule.live.intro.LiveIntroComponent;
import com.bokecc.livemodule.live.morefunction.MoreFunctionLayout;
import com.bokecc.livemodule.live.morefunction.rtc.RTCVideoLayout;
import com.bokecc.livemodule.live.qa.LiveQAComponent;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.live.video.LiveVideoView;
import com.bokecc.livemodule.utils.TimeUtil;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.OnMarqueeImgFailListener;
import com.bokecc.sdk.mobile.live.pojo.Marquee;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.widget.MarqueeView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_IMAGE_COMPONENT;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_ULR_COMPONET;

/**
 * 直播播放页（默认视频大屏，文档小屏，可手动切换）
 */
public class LivePlayActivity extends BaseActivity implements DWLiveBarrageListener, DWLiveRTCListener {
    View mRoot;
    RelativeLayout mLiveTopLayout;
    RelativeLayout mLiveMsgLayout;
    // 大屏视频或文档布局
    RelativeLayout mLiveVideoContainer;
    // 跑马灯组件
    MarqueeView mMarqueeView;
    // 弹幕组件
    BarrageLayout mLiveBarrage;
    // 直播视频View
    LiveVideoView mLiveVideoView;
    // 连麦视频View
    RTCVideoLayout mLiveRtcView;
    // 直播间状态布局
    LiveRoomLayout mLiveRoomLayout;
    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mLiveFloatingView;
    // 直播功能处理机制（签到、答题卡/投票、问卷、抽奖）
    FunctionHandler mFunctionHandler;
    // 更多功能控件（私聊、连麦、公告）
    MoreFunctionLayout mMoreFunctionLayout;


    private KeyboardHeightProvider keyboardHeightProvider;

    // 直播未开始
    private RelativeLayout mNoStreamRoot;
    private TextView mNoStreamText;
    private TextView mCountDownTimeText;


    private CountDownTimer mCountDownTimer;
    private RoomInfo roomInfo;
    boolean isOpenMarquee; // 是否显示跑马灯
    private RelativeLayout video_root;
    private FloatingPopupWindow.FloatDismissListener floatDismissListener = new FloatingPopupWindow.FloatDismissListener() {
        @Override
        public void dismiss() {
            if (mLiveRoomLayout.viewState == LiveRoomLayout.State.VIDEO) {
                mLiveRoomLayout.setSwitchText(LiveRoomLayout.State.OPEN_DOC);
            } else if (mLiveRoomLayout.viewState == LiveRoomLayout.State.DOC) {
                mLiveRoomLayout.setSwitchText(LiveRoomLayout.State.OPEN_VIDEO);
            }
        }
    };
    private TextView mLandVote;
    private TextView mPortraitVote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 请求全屏
        requestFullScreenFeature();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_play);

        initViews();
        initViewPager();
        initRoomStatusListener();
        mFunctionHandler = new FunctionHandler();
        mFunctionHandler.initFunctionHandler(this,functionCallBack);
        keyboardHeightProvider = new KeyboardHeightProvider(this);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                keyboardHeightProvider.start();
            }
        });

        roomInfo = DWLive.getInstance().getRoomInfo();

        if (roomInfo != null) {
            // 获取是否显示弹幕
            isBarrageOn = roomInfo.getBarrage() == 1;
            mLiveRoomLayout.controlBarrageControl(isBarrageOn);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        keyboardHeightProvider.addKeyboardHeightObserver(mChatLayout);
        keyboardHeightProvider.addKeyboardHeightObserver(mLiveRoomLayout);
        keyboardHeightProvider.addKeyboardHeightObserver(mQaLayout);
        keyboardHeightProvider.addKeyboardHeightObserver(mMoreFunctionLayout);
        mFunctionHandler.setRootView(mRoot);
        // 判断是否开启了弹幕
        if (isBarrageOn && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLiveBarrage.start();
        }
        // 开始播放
        mLiveVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        keyboardHeightProvider.clearObserver();
        mFunctionHandler.removeRootView();
        // 停止直播
        mLiveVideoView.stop();
        // 停止弹幕
        mLiveBarrage.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.close();
        mLiveFloatingView.dismiss();
        mFunctionHandler.onDestroy(this);
        if (DWLiveCoreHandler.getInstance().isRtcing()) {
            DWLive.getInstance().disConnectSpeak();
            mLiveVideoView.stop();
        }
        if (mLiveRtcView != null) {
            mLiveRtcView.destroy();
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mLiveVideoView.destroy();
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            quitFullScreen();
            return;
        } else {
            if (mChatLayout != null && mChatLayout.onBackPressed()) {
                return;
            }
        }
        // 弹出退出提示
        showExitTips();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏隐藏状态栏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(true));
            if (mLiveBarrage != null && isBarrageOn) {
                mLiveBarrage.start();
            }
            //如果随堂测 答题卡的缩小按钮存在
            if (mPortraitVote.getVisibility()==VISIBLE){
                mPortraitVote.setVisibility(View.GONE);
                mLandVote.setVisibility(VISIBLE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
            if (mLiveBarrage != null) {
                mLiveBarrage.stop();
            }
            if (mLandVote.getVisibility()==VISIBLE){
                mLandVote.setVisibility(View.GONE);
                mPortraitVote.setVisibility(VISIBLE);
            }
        }
        //调整窗口的位置
        if (mLiveFloatingView != null) {
            mLiveFloatingView.onConfigurationChanged(newConfig.orientation);
        }
    }

    @TargetApi(19)
    private static int getSystemUiVisibility(boolean isFull) {
        if (isFull) {
            int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            return flags;
        } else {
            return View.SYSTEM_UI_FLAG_VISIBLE;
        }

    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mLiveTopLayout = findViewById(R.id.rl_pc_live_top_layout);
        video_root = findViewById(R.id.video_root);
        mLiveVideoContainer = findViewById(R.id.rl_video_container);
        mLiveVideoView = findViewById(R.id.live_video_view);
        mLiveRoomLayout = findViewById(R.id.live_room_layout);
        mLiveRoomLayout.setVideo(mLiveVideoView);
        mLiveBarrage = findViewById(R.id.live_barrage);
        mNoStreamRoot = findViewById(R.id.no_stream_root);
        mNoStreamText = findViewById(R.id.tv_no_stream);
        mCountDownTimeText = findViewById(R.id.id_count_down_time);
        // 视频下方界面
        mLiveMsgLayout = findViewById(R.id.ll_pc_live_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);
        mMoreFunctionLayout = findViewById(R.id.more_function_layout);

        // 弹出框界面
        mExitPopupWindow = new ExitPopupWindow(this);
        mLiveFloatingView = new FloatingPopupWindow(LivePlayActivity.this);
        mLiveFloatingView.setFloatDismissListener(floatDismissListener);
        // 连麦相关
        mLiveRtcView = findViewById(R.id.live_rtc_view);
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setDwLiveRTCListener(this);
        }
        //随堂测 答题卡缩小的按钮
        mLandVote = findViewById(R.id.tv_land_vote);
        mLandVote.setOnClickListener(voteClickListener);
        mPortraitVote = findViewById(R.id.tv_portrait_vote);
        mPortraitVote.setOnClickListener(voteClickListener);
        // 检测权限（用于连麦）
        doPermissionCheck();
        //首次进入默认竖屏 所以需要关闭弹幕
        mLiveBarrage.stop();
    }

    /**
     * 进行权限检测
     */
    private void doPermissionCheck() {
        Permissions.request(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new Permissions.Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == PackageManager.PERMISSION_GRANTED) {
                            //Toast.makeText(LivePlayActivity.this, "Permission Allow", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LivePlayActivity.this, "请开启相关权限", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    //---------------------------------- 直播间状态监听 --------------------------------------------/

    LiveRoomLayout.LiveRoomStatusListener roomStatusListener = new LiveRoomLayout.LiveRoomStatusListener() {

        // 文档/视频布局区域 回调事件 #Called From LiveRoomLayout
        @Override
        public synchronized void switchVideoDoc(final LiveRoomLayout.State state) {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler == null) {
                return;
            }
            // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
            if (dwLiveCoreHandler.hasPdfView() && mLiveFloatingView != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (state == LiveRoomLayout.State.VIDEO) {
                            //如果当前小窗口开启并且大窗口是视频 将大窗口切换到文档
                            switchView(true);
                        } else if (state == LiveRoomLayout.State.DOC) {
                            switchView(false);
                        } else if (state == LiveRoomLayout.State.OPEN_DOC) {
                            mLiveFloatingView.show(mRoot);
                            if (mDocLayout.getParent() != null)
                                ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
                            mLiveFloatingView.addView(mDocLayout);
                        } else if (state == LiveRoomLayout.State.OPEN_VIDEO) {
                            mLiveFloatingView.show(mRoot);
                            if (mLiveVideoContainer.getParent() != null)
                                ((ViewGroup) mLiveVideoContainer.getParent()).removeView(mLiveVideoContainer);
                            mLiveFloatingView.addView(mLiveVideoContainer);
                        }
                    }
                });
            }
        }

        private void switchView(boolean isVideoMain) {
            if (mDocLayout.getParent() != null)
                ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
            if (mLiveVideoContainer.getParent() != null)
                ((ViewGroup) mLiveVideoContainer.getParent()).removeView(mLiveVideoContainer);
            mLiveVideoView.setShowSpeed(!isVideoMain);
            if (DWLiveCoreHandler.getInstance().isRtcing()) {//连麦中切换窗口
                if (isVideoMain) {
                    mLiveFloatingView.addView(mDocLayout);
                    video_root.addView(mLiveVideoContainer, 0);
                    mDocLayout.setDocScrollable(false);//设置webview不可滑动
                    mLiveVideoContainer.invalidate();
                } else {
                    mLiveFloatingView.addView(mLiveVideoContainer);
                    ViewGroup.LayoutParams lp = mDocLayout.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    mDocLayout.setLayoutParams(lp);
                    video_root.addView(mDocLayout, 0);
                    mDocLayout.setDocScrollable(true);//设置webview可滑动
                }
            } else {//ijkplayer拉流切换窗口
                if (isVideoMain) {
                    mLiveFloatingView.addView(mDocLayout);
                    video_root.addView(mLiveVideoContainer, 0);
                    mDocLayout.setDocScrollable(false);//设置webview不可滑动
                } else {
                    mLiveFloatingView.addView(mLiveVideoContainer);
//                                ViewGroup.LayoutParams lp = mDocLayout.getLayoutParams();
//                                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                                mDocLayout.setLayoutParams(lp);
                    video_root.addView(mDocLayout, 0);
                    mDocLayout.setDocScrollable(true);//设置webview可滑动
                }
            }
        }

        // 退出直播间
        @Override
        public void closeRoom() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 如果当前状态是竖屏，则弹出退出确认框，否则切换为竖屏
                    if (isPortrait()) {
                        showExitTips();
                    } else {
                        quitFullScreen();
                    }
                }
            });
        }

        // 全屏
        @Override
        public void fullScreen() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mLiveMsgLayout.setVisibility(View.GONE);
                }
            });
        }

        // 踢出直播间
        @Override
        public void kickOut() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LivePlayActivity.this, "您已经被踢出直播间", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        @Override
        public void onClickDocScaleType(int scaleType) {
            if (mDocLayout != null) {
                mDocLayout.setScaleType(scaleType);
            }
        }
    };

    // 初始化房间状态监听
    private void initRoomStatusListener() {
        if (mLiveRoomLayout == null) {
            return;
        }
        mLiveRoomLayout.setLiveRoomStatusListener(roomStatusListener);
        mLiveVideoView.setOnStreamCallback(new LiveVideoView.OnStreamCallback() {
            @Override
            public void onStreamEnd(boolean isNormal) {
                mNoStreamRoot.setVisibility(VISIBLE);
                if (mLiveRoomLayout.getFullView() != null) {
                    mLiveRoomLayout.getFullView().setVisibility(View.GONE);
                }
                //隐藏全凭按钮
                if (isNormal) {
                    mNoStreamText.setText("直播已结束");
                } else {
                    mNoStreamText.setText("直播未开始");
                    // 如果开启了倒计时，则显示
                    if (roomInfo != null && roomInfo.getOpenLiveCountdown() == 1) {
                        long downTime = roomInfo.getLiveCountdown() * 1000;
                        mCountDownTimer = new CountDownTimer(downTime, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                mCountDownTimeText.setText(TimeUtil.getYearMonthDayHourMinuteSecond(LivePlayActivity.this, millisUntilFinished / 1000));
                            }

                            @Override
                            public void onFinish() {
                                mCountDownTimeText.setVisibility(View.GONE);
                            }
                        };
                        mCountDownTimer.start();
                    }


                }
                //初始化视频界面并隐藏悬浮框
//                roomStatusListener.switchVideoDoc(true);
                if (mLiveFloatingView != null) {
                    mLiveFloatingView.dismiss();
                }
                //
                closeMarquee();
            }

            @Override
            public void onStreamStart() {
                mNoStreamRoot.setVisibility(View.GONE);
                //显示全凭按钮
                if (mLiveRoomLayout.getFullView() != null) {
                    mLiveRoomLayout.getFullView().setVisibility(VISIBLE);
                }
                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                    mCountDownTimer = null;
                }
                //显示文档
                if (DWLiveCoreHandler.getInstance().hasPdfView()) {
                    showFloatingDocLayout();
                    roomStatusListener.switchVideoDoc(mLiveRoomLayout.isVideoMain());
                }
                //开启跑马灯
                openMarquee();
                /**
                 * 尝试获取一下当前正在进行的随堂测，需要考虑用户重新登录及Home桌面的问题
                 */
                if (DWLive.getInstance() != null) {
                    DWLive.getInstance().getPracticeInformation();
                }
            }
        });
    }

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mLiveMsgLayout.setVisibility(VISIBLE);
        mLiveRoomLayout.quitFullScreen();
    }

    //---------------------------------- 连麦状态监听 --------------------------------------------/

    /**
     * isVideoRtc always is true
     *
     * @param isVideoRtc 当前连麦是否是视频连麦
     * @param videoSize  视频的宽高，值为"600x400"
     */
    @Override
    public void onEnterSpeak(final boolean isVideoRtc, final boolean needAdjust, final String videoSize) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomStatusListener.switchVideoDoc(mLiveRoomLayout.viewState);
                if (mLiveVideoView != null) {
                    mLiveVideoView.enterRtcMode(isVideoRtc);
                }
                if (mLiveRtcView != null) {
                    mLiveRtcView.enterSpeak(isVideoRtc, needAdjust, videoSize);
                }
            }
        });
    }

    @Override
    public void onDisconnectSpeak() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLiveRtcView != null) {
                    mLiveRtcView.disconnectSpeak();
                }
                if (mLiveVideoView != null) {
                    roomStatusListener.switchVideoDoc(mLiveRoomLayout.isVideoMain());
                    mLiveVideoView.exitRtcMode();
                }
            }
        });
    }

    @Override
    public void onSpeakError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLiveRtcView != null) {
                    mLiveRtcView.speakError(e);
                }
                if (mLiveVideoView != null) {
                    mLiveVideoView.exitRtcMode();
                }
            }
        });
    }

    //---------------------------------- 弹幕控制监听 --------------------------------------------/

    // 弹幕开关的标志
    boolean isBarrageOn = true;

    /**
     * 收到弹幕开启事件
     */
    @Override
    public void onBarrageOn() {
        if (mLiveBarrage != null) {
            mLiveBarrage.start();
            isBarrageOn = true;
        }
    }

    /**
     * 收到弹幕关闭事件
     */
    @Override
    public void onBarrageOff() {
        if (mLiveBarrage != null) {
            mLiveBarrage.stop();
            isBarrageOn = false;
        }
    }


    //*************************************** 下方布局 ***************************************/

    List<View> mLiveInfoList = new ArrayList<>();
    List<Integer> mIdList = new ArrayList<>();
    List<RadioButton> mTagList = new ArrayList<>();

    ViewPager mViewPager;

    RadioGroup mRadioGroup;
    RadioButton mIntroTag;
    RadioButton mQaTag;
    RadioButton mChatTag;
    RadioButton mDocTag;

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        initComponents();
        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mLiveInfoList.size();
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mLiveInfoList.get(position));
                return mLiveInfoList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mLiveInfoList.get(position));
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTagList.get(position).setChecked(true);
                hideKeyboard();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                mViewPager.setCurrentItem(mIdList.indexOf(i), true);
            }
        });
        if (mTagList != null && mTagList.size() > 0) {
            mTagList.get(0).performClick();
        }
    }

    /********************************* 直播 问答、聊天、文档、简介 组件相关 ***************************************/

    // 简介组件
    LiveIntroComponent mIntroComponent;
    // 问答组件
    LiveQAComponent mQaLayout;
    // 聊天组件
    LiveChatComponent mChatLayout;
    // 文档组件
    LiveDocComponent mDocLayout;

    //根据直播间模版初始化相关组件
    private void initComponents() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        // 判断当前直播间模版是否有"文档"功能
        if (dwLiveCoreHandler.hasPdfView()) {
            initDocLayout();
        }
        // 判断当前直播间模版是否有"聊天"功能
        if (dwLiveCoreHandler.hasChatView()) {
            initChatLayout();
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwLiveCoreHandler.hasQaView()) {
            initQaLayout();
        }
        // 判断当前直播间模版是否是视频大屏模式，如果是，隐藏更多功能
        if (dwLiveCoreHandler.isOnlyVideoTemplate()) {
            mMoreFunctionLayout.setVisibility(View.GONE);
        }
        // 直播间简介
        initIntroLayout();
        // 设置弹幕状态监听
        dwLiveCoreHandler.setDwLiveBarrageListener(this);

    }

    public void openMarquee() {
        if (DWLive.getInstance().getRoomInfo() != null) {
            isOpenMarquee = DWLive.getInstance().getRoomInfo().getOpenMarquee() == 1;
        }

        if (isOpenMarquee) {
            //设置跑马灯
            mMarqueeView = findViewById(R.id.marquee_view);
            mMarqueeView.setVisibility(VISIBLE);
            setMarquee((Marquee) getIntent().getSerializableExtra("marquee"));
        }
    }

    public void closeMarquee() {
        if (mMarqueeView != null) {
            mMarqueeView.stop();
            mMarqueeView.setVisibility(View.GONE);
        }
    }

    public void setMarquee(final Marquee marquee) {
        final ViewGroup parent = (ViewGroup) mMarqueeView.getParent();
        if (parent.getWidth() != 0 && parent.getHeight() != 0) {
            if (marquee != null && marquee.getAction() != null) {
                if (marquee.getType().equals("text")) {
                    mMarqueeView.setTextContent(marquee.getText().getContent());
                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(this, marquee.getText().getFont_size()));
                    mMarqueeView.setType(1);
                } else {
                    mMarqueeView.setMarqueeImage(this, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                    mMarqueeView.setType(2);
                }
                mMarqueeView.setMarquee(marquee, parent.getHeight(), parent.getWidth());
                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                    @Override
                    public void onLoadMarqueeImgFail() {
                        //跑马灯加载失败
                        toastOnUiThread("跑马灯加载失败");
                    }
                });
                mMarqueeView.start();
            }
        } else {
            parent.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= 16) {
                                parent.getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                            } else {
                                parent.getViewTreeObserver()
                                        .removeGlobalOnLayoutListener(this);
                            }
                            int width = parent.getWidth();// 获取宽度
                            int height = parent.getHeight();// 获取高度
                            if (marquee != null && marquee.getAction() != null) {
                                if (marquee.getType().equals("text")) {
                                    mMarqueeView.setTextContent(marquee.getText().getContent());
                                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(LivePlayActivity.this, marquee.getText().getFont_size()));
                                    mMarqueeView.setType(1);
                                } else {
                                    mMarqueeView.setMarqueeImage(LivePlayActivity.this, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                                    mMarqueeView.setType(2);
                                }
                                mMarqueeView.setMarquee(marquee, height, width);
                                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                                    @Override
                                    public void onLoadMarqueeImgFail() {
                                        //跑马灯加载失败
                                        toastOnUiThread("跑马灯加载失败");
                                    }
                                });
                                mMarqueeView.start();
                            }
                        }
                    });
        }

    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(VISIBLE);
        mIntroComponent = new LiveIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(VISIBLE);
        mQaLayout = new LiveQAComponent(this);
        mLiveInfoList.add(mQaLayout);
        mLiveRoomLayout.setPopView(mRoot);
    }

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(VISIBLE);
        mChatLayout = new LiveChatComponent(this);
        mChatLayout.setPopView(mRoot);
        //initChatView();
        mLiveInfoList.add(mChatLayout);
        mChatLayout.setBarrageLayout(mLiveBarrage);
        mChatLayout.setOnChatComponentClickListener(new OnChatComponentClickListener() {
            @Override
            public void onClickChatComponent(Bundle bundle) {
                if (bundle == null) return;
                String type = bundle.getString("type");
                if (CONTENT_IMAGE_COMPONENT.equals(type)) {
                    Intent intent = new Intent(LivePlayActivity.this, ImageDetailsActivity.class);
                    intent.putExtra("imageUrl", bundle.getString("url"));
                    startActivity(intent);
                } else if (CONTENT_ULR_COMPONET.equals(type)) {
                    Uri uri = Uri.parse(bundle.getString("url"));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }


            }
        });
    }


    // 初始化文档布局区域
    private void initDocLayout() {
        mDocLayout = new LiveDocComponent(this);
        mLiveFloatingView.addView(mDocLayout);
    }

    // 展示文档悬浮窗布局
    private void showFloatingDocLayout() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
        if (dwLiveCoreHandler.hasPdfView()) {
            mLiveFloatingView.show(mRoot);
        }
    }

    //********************************** 工具方法 *******************************************/

    // 隐藏输入法
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mLiveTopLayout.getWindowToken(), 0);
        }
    }


    //*********************************  退出相关逻辑 ***************************************/
    private ExitPopupWindow mExitPopupWindow;

    // 弹出退出提示
    private void showExitTips() {
        if (mExitPopupWindow != null) {
            mExitPopupWindow.setConfirmExitRoomListener(new ExitPopupWindow.ConfirmExitRoomListener() {
                @Override
                public void onConfirmExitRoom() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mExitPopupWindow.dismiss();
                            finish();
                        }
                    });
                }
            });
            mExitPopupWindow.show(mRoot);
        }
    }
    private boolean isVote;
    //添加答题卡收起监听
    private FunctionCallBack functionCallBack = new FunctionCallBack() {
        @Override
        public void onMinimize(boolean isVote) {
            super.onMinimize(isVote);
            LivePlayActivity.this.isVote = isVote;
            if (isVote){
                mPortraitVote.setBackgroundResource(R.drawable.float_answer2);
                mLandVote.setBackgroundResource(R.drawable.float_answer2);
            }else{
                mPortraitVote.setBackgroundResource(R.drawable.float_answer);
                mLandVote.setBackgroundResource(R.drawable.float_answer);
            }
            //显示按钮
            if (isPortrait()){
                mPortraitVote.setVisibility(VISIBLE);
                mLandVote.setVisibility(View.GONE);
            }else{
                mLandVote.setVisibility(VISIBLE);
                mPortraitVote.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClose() {
            super.onClose();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //隐藏按钮
                    mPortraitVote.setVisibility(View.GONE);
                    mLandVote.setVisibility(View.GONE);
                }
            });
        }
    };
    //随堂测 答题卡缩小按钮的点击时间
    View.OnClickListener voteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFunctionHandler!=null){
                if (isVote){
                    mFunctionHandler.onVoteStart();
                }else{
                    mFunctionHandler.onPractic();
                }
            }
            mPortraitVote.setVisibility(View.GONE);
            mLandVote.setVisibility(View.GONE);
        }
    };
}
