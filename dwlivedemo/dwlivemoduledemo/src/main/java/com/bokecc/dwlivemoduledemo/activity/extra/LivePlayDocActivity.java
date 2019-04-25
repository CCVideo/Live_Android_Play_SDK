package com.bokecc.dwlivemoduledemo.activity.extra;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.dwlivemoduledemo.popup.ExitPopupWindow;
import com.bokecc.dwlivemoduledemo.popup.FloatingPopupWindow;
import com.bokecc.livemodule.live.DWLiveBarrageListener;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.chat.LiveChatComponent;
import com.bokecc.livemodule.live.chat.barrage.BarrageLayout;
import com.bokecc.livemodule.live.doc.LiveDocComponent;
import com.bokecc.livemodule.live.intro.LiveIntroComponent;
import com.bokecc.livemodule.live.qa.LiveQAComponent;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.live.video.LiveVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播播放页（默认文档大屏，视频小屏，可手动切换）
 *
 * 注意：此播放页只适配直播间模版中有'文档'区域的
 */
public class LivePlayDocActivity extends BaseActivity implements DWLiveBarrageListener {

    View mRoot;

    RelativeLayout mLiveTopLayout;

    LinearLayout mLiveMsgLayout;

    RelativeLayout mLiveVideoContainer;
    // 弹幕组件
    BarrageLayout mLiveBarrage;
    // 直播视频View
    LiveVideoView mLiveVideoView;
    // 直播文档View
    LiveDocComponent mLiveDocView;
    // 直播间状态布局
    LiveRoomLayout mLiveRoomLayout;
    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mLiveFloatingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 隐藏ActionBar
        hideActionBar();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_play_doc);
        initViews();
        initViewPager();
        initRoomStatusListener();
    }

    // 首次由surface创建启动生命周期，再后面由onResume和onSurfaceTextureAvailable来控制生命周期
    boolean isFirstResume = true;

    @Override
    protected void onResume() {
        super.onResume();
        // 判断是否开启了弹幕
        if (isBarrageOn) {
            mLiveBarrage.start();
        }
        // 展示悬浮窗
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstResume) {
                    showFloatingLayout();
                    isFirstResume = false;
                } else {
                    mLiveVideoView.start();
                    showFloatingLayout();
                }
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveVideoView.stop();
        mLiveBarrage.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveFloatingView.dismiss();
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
        if (mExitPopupWindow != null) {
            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
            mExitPopupWindow.show(mRoot);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLiveBarrage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mLiveBarrage.init();
    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mLiveTopLayout = findViewById(R.id.rl_pc_live_top_layout);
        mLiveVideoContainer = findViewById(R.id.rl_video_container);
        mLiveDocView = findViewById(R.id.live_doc_view);
        mLiveRoomLayout = findViewById(R.id.live_room_layout);
        mLiveBarrage = findViewById(R.id.live_barrage);
        // 视频下方界面
        mLiveMsgLayout = findViewById(R.id.ll_pc_live_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);
        // 弹出框界面
        mExitPopupWindow = new ExitPopupWindow(this);
        mLiveFloatingView = new FloatingPopupWindow(LivePlayDocActivity.this);

        // 修改初始的直播间状态，视频小屏
        mLiveRoomLayout.setVideoDocSwitchStatus(false);
    }



    //---------------------------------- 直播间状态监听 --------------------------------------------/

    LiveRoomLayout.LiveRoomStatusListener roomStatusListener = new LiveRoomLayout.LiveRoomStatusListener() {

        // 文档/视频布局区域 回调事件 #Called From LiveRoomLayout
        @Override
        public void switchVideoDoc(final boolean videoMain) {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler == null) {
                return;
            }
            // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
            if (dwLiveCoreHandler.hasPdfView()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (videoMain) {
                            mLiveVideoContainer.removeAllViews();
                            mLiveFloatingView.removeAllView();
                            mLiveFloatingView.addView(mLiveDocView);
                            mLiveVideoContainer.addView(mLiveVideoView);
                        } else {
                            mLiveVideoContainer.removeAllViews();
                            mLiveFloatingView.removeAllView();
                            mLiveFloatingView.addView(mLiveVideoView);
                            mLiveVideoContainer.addView(mLiveDocView);
                        }
                    }
                });
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
                    Toast.makeText(LivePlayDocActivity.this, "您已经被踢出直播间", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    };

    // 初始化房间状态监听
    private void initRoomStatusListener() {
        if (mLiveRoomLayout == null) {
            return;
        }
        mLiveRoomLayout.setLiveRoomStatusListener(roomStatusListener);
    }

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mLiveMsgLayout.setVisibility(View.VISIBLE);
        mLiveRoomLayout.quitFullScreen();
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

    /********************************* 直播重要组件相关 ***************************************/

    // 简介组件
    LiveIntroComponent mIntroComponent;
    // 问答组件
    LiveQAComponent mQaLayout;
    // 聊天组件
    LiveChatComponent mChatLayout;

    /**
     * 根据直播间模版初始化相关组件
     */
    private void initComponents() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        // 初始化视频区域（悬浮小窗）
        initVideoLayout();

        // 判断当前直播间模版是否有"聊天"功能
        if (dwLiveCoreHandler.hasChatView()) {
            initChatLayout();
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwLiveCoreHandler.hasQaView()) {
            initQaLayout();
        }
        // 直播间简介
        initIntroLayout();
        // 设置弹幕状态监听
        dwLiveCoreHandler.setDwLiveBarrageListener(this);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(View.VISIBLE);
        mIntroComponent = new LiveIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(View.VISIBLE);
        mQaLayout = new LiveQAComponent(this);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(View.VISIBLE);
        mChatLayout = new LiveChatComponent(this);
        mLiveInfoList.add(mChatLayout);
        mChatLayout.setBarrageLayout(mLiveBarrage);
    }

    // 初始化视频布局区域
    private void initVideoLayout() {
        mLiveVideoView = new LiveVideoView(this);
        mLiveFloatingView.addView(mLiveVideoView);
    }

    // 展示悬浮窗布局
    private void showFloatingLayout() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        if (!mLiveFloatingView.isShowing()) {
            mLiveFloatingView.show(mRoot);
        }
    }

    /********************************* 工具方法 ***************************************/

    // 隐藏输入法
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mLiveTopLayout.getWindowToken(), 0);
        }
    }

}