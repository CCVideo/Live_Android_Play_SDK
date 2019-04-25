package com.bokecc.dwlivemoduledemo.activity.extra;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.dwlivemoduledemo.popup.ExitPopupWindow;
import com.bokecc.dwlivemoduledemo.popup.FloatingPopupWindow;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.chat.ReplayChatComponent;
import com.bokecc.livemodule.replay.doc.ReplayDocComponent;
import com.bokecc.livemodule.replay.intro.ReplayIntroComponent;
import com.bokecc.livemodule.replay.qa.ReplayQAComponent;
import com.bokecc.livemodule.replay.room.ReplayRoomLayout;
import com.bokecc.livemodule.replay.video.ReplayVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * 回放播放页 （默认文档大屏，视频小屏，可手动切换）
 *
 * 注意：此播放页只适配直播间模版中有'文档'区域的
 */
public class ReplayPlayDocActivity extends BaseActivity {

    View mRoot;

    LinearLayout mReplayMsgLayout;

    RelativeLayout mReplayVideoContainer;
    // 回放视频View
    ReplayVideoView mReplayVideoView;
    // 回放文档View
    ReplayDocComponent mReplayDocView;

    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mReplayFloatingView;

    ReplayRoomLayout mReplayRoomLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏状态栏
        hideActionBar();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_play_doc);
        initViews();
        initViewPager();
    }

    // 首次由surface创建启动生命周期，再后面由onResume和onSurfaceTextureAvailable来控制生命周期
    boolean isFirstResume = true;

    @Override
    protected void onResume() {
        super.onResume();
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 展示悬浮窗
                if (isFirstResume) {
                    showFloatingLayout();
                    isFirstResume = false;
                } else {
                    mReplayVideoView.start();
                    showFloatingLayout();
                }
            }
        }, 200);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReplayVideoView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReplayFloatingView.dismiss();
        mReplayVideoView.destory();
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            quitFullScreen();
            return;
        }
        if (mExitPopupWindow != null) {
            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
            mExitPopupWindow.show(mRoot);
        }
    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mReplayVideoContainer = findViewById(R.id.rl_video_container);
        mReplayDocView = findViewById(R.id.replay_doc_view);
        mReplayRoomLayout = findViewById(R.id.replay_room_layout);

        mReplayMsgLayout = findViewById(R.id.ll_pc_replay_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);

        // 弹出框界面
        mExitPopupWindow = new ExitPopupWindow(this);
        mReplayFloatingView = new FloatingPopupWindow(this);

        mReplayRoomLayout.setVideoDocSwitchText("切换视频");

        mReplayRoomLayout.setReplayRoomStatusListener(roomStatusListener);
    }

    /**
     * 根据直播间模版初始化相关组件
     */
    private void initComponents() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler == null) {
            return;
        }

        initVideoLayout();

        // 判断当前直播间模版是否有"聊天"功能
        if (dwReplayCoreHandler.hasChatView()) {
            initChatLayout();
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwReplayCoreHandler.hasQaView()) {
            initQaLayout();
        }
        // 直播间简介
        initIntroLayout();
    }

    /********************************* 直播重要组件相关 ***************************************/

    // 简介组件
    ReplayIntroComponent mIntroComponent;

    // 问答组件
    ReplayQAComponent mQaLayout;

    // 聊天组件
    ReplayChatComponent mChatLayout;

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(View.VISIBLE);
        mChatLayout = new ReplayChatComponent(this);
        mLiveInfoList.add(mChatLayout);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(View.VISIBLE);
        mQaLayout = new ReplayQAComponent(this);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(View.VISIBLE);
        mIntroComponent = new ReplayIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化视频布局区域
    private void initVideoLayout() {
        mReplayVideoView = new ReplayVideoView(this);
        mReplayFloatingView.addView(mReplayVideoView);
    }

    // 展示悬浮窗布局
    private void showFloatingLayout() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler == null) {
            return;
        }
        if (!mReplayFloatingView.isShowing()) {
            mReplayFloatingView.show(mRoot);
        }
    }

    /*************************************** 下方布局 ***************************************/

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

    /**************************************  Room 状态回调监听 *************************************/

    boolean isVideoMain = false;

    private ReplayRoomLayout.ReplayRoomStatusListener roomStatusListener = new ReplayRoomLayout.ReplayRoomStatusListener() {

        @Override
        public void switchVideoDoc() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isVideoMain) {
                        mReplayVideoContainer.removeAllViews();
                        mReplayFloatingView.removeAllView();
                        mReplayFloatingView.addView(mReplayVideoView);
                        mReplayVideoContainer.addView(mReplayDocView);
                        isVideoMain = false;
                        mReplayRoomLayout.setVideoDocSwitchText("切换视频");
                    } else {
                        mReplayVideoContainer.removeAllViews();
                        mReplayFloatingView.removeAllView();
                        mReplayFloatingView.addView(mReplayDocView);
                        mReplayVideoContainer.addView(mReplayVideoView);
                        isVideoMain = true;
                        mReplayRoomLayout.setVideoDocSwitchText("切换文档");
                    }
                }
            });
        }

        @Override
        public void closeRoom() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 如果当前状态是竖屏，则弹出退出确认框，否则切换为竖屏
                    if (isPortrait()) {
                        if (mExitPopupWindow != null) {
                            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
                            mExitPopupWindow.show(mRoot);
                        }
                    } else {
                        quitFullScreen();
                    }
                }
            });
        }

        @Override
        public void fullScreen() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mReplayMsgLayout.setVisibility(View.GONE);
                }
            });
        }
    };

    //---------------------------------- 全屏相关逻辑 --------------------------------------------/

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mReplayMsgLayout.setVisibility(View.VISIBLE);
        mReplayRoomLayout.quitFullScreen();
    }

    //---------------------------------- 退出相关逻辑 --------------------------------------------/

    ExitPopupWindow mExitPopupWindow;

    ExitPopupWindow.ConfirmExitRoomListener confirmExitRoomListener = new ExitPopupWindow.ConfirmExitRoomListener() {
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
    };

}