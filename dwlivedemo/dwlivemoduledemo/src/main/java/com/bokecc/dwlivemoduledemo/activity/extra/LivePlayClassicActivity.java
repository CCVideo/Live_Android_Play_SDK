package com.bokecc.dwlivemoduledemo.activity.extra;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.dwlivemoduledemo.popup.ExitPopupWindow;
import com.bokecc.livemodule.live.DWLiveBarrageListener;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.chat.LiveChatComponent;
import com.bokecc.livemodule.live.chat.barrage.BarrageLayout;
import com.bokecc.livemodule.live.doc.LiveDocComponent;
import com.bokecc.livemodule.live.function.FunctionHandler;
import com.bokecc.livemodule.live.intro.LiveIntroComponent;
import com.bokecc.livemodule.live.morefunction.MoreFunctionLayout;
import com.bokecc.livemodule.live.qa.LiveQAComponent;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.live.video.LiveVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播经典播放页（默认视频在上，文档在下，不建议做手动切换）
 */
public class LivePlayClassicActivity extends BaseActivity implements DWLiveBarrageListener {


    View mRoot;
    RelativeLayout mLiveTopLayout;
    RelativeLayout mLiveMsgLayout;
    RelativeLayout mLiveVideoContainer;
    // 弹幕组件
    BarrageLayout mLiveBarrage;
    // 直播视频View
    LiveVideoView mLiveVideoView;
    // 直播间状态布局
    LiveRoomLayout mLiveRoomLayout;
    // 直播功能处理机制（签到、答题卡/投票、问卷、抽奖）
    FunctionHandler mFunctionHandler;
    // 更多功能控件（私聊、连麦、公告）
    MoreFunctionLayout mMoreFunctionLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 隐藏ActionBar
        hideActionBar();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_play_classic);
        initViews();
        initViewPager();
        initRoomStatusListener();
        mFunctionHandler = new FunctionHandler();
        mFunctionHandler.initFunctionHandler(this);
    }

    @Override
    protected void onResume() {
//        LogHelper.getInstance().writeLog("LivePlayActivity onResume");
        super.onResume();
        mFunctionHandler.setRootView(mRoot);
        // 判断是否开启了弹幕
        if (isBarrageOn) {
            mLiveBarrage.start();
        }
        // 展示悬浮窗
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLiveVideoView.start();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
//        LogHelper.getInstance().writeLog("LivePlayActivity onPause");
        super.onPause();
        mFunctionHandler.removeRootView();
        mLiveVideoView.stop();
        mLiveBarrage.stop();
    }

    @Override
    protected void onDestroy() {
//        LogHelper.getInstance().writeLog("LivePlayActivity onDestroy");
        super.onDestroy();
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
        mLiveVideoView = findViewById(R.id.live_video_view);
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
        mMoreFunctionLayout = findViewById(R.id.more_function_layout);

        // 弹出框界面
        mExitPopupWindow = new ExitPopupWindow(this);

        // 检测权限（用于连麦）
        doPermissionCheck();
    }

    /**
     * 进行权限检测
     */
    private void doPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();
            // 申请 相机 麦克风权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Allow
                Toast.makeText(LivePlayClassicActivity.this, "Permission Allow", Toast.LENGTH_SHORT).show();
            } else {
                // Permission Denied
                Toast.makeText(LivePlayClassicActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //---------------------------------- 直播间状态监听 --------------------------------------------/

    LiveRoomLayout.LiveRoomStatusListener roomStatusListener = new LiveRoomLayout.LiveRoomStatusListener() {

        // 文档/视频布局区域 回调事件 #Called From LiveRoomLayout
        @Override
        public void switchVideoDoc(final boolean videoMain) {
            // TODO 不建议在此页面布局类型下做切换操作，如需要,请自行添加完善逻辑
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
                    Toast.makeText(LivePlayClassicActivity.this, "您已经被踢出直播间", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        @Override
        public void onClickDocScaleType(int scaleType) {
            
        }
    };

    // 初始化房间状态监听
    private void initRoomStatusListener() {
        if (mLiveRoomLayout == null) {
            return;
        }
        mLiveRoomLayout.setLiveRoomStatusListener(roomStatusListener);
        // 因为不建议在此界面模板做切换文档视频操作，所以隐藏此按钮
        mLiveRoomLayout.hideSwitchVideoDoc();
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

    /********************************* 直播 问答、聊天、文档、简介 组件相关 ***************************************/

    // 简介组件
    LiveIntroComponent mIntroComponent;
    // 问答组件
    LiveQAComponent mQaLayout;
    // 聊天组件
    LiveChatComponent mChatLayout;
    // 文档组件
    LiveDocComponent mDocLayout;

    /**
     * 根据直播间模版初始化相关组件
     */
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

    // 初始化文档布局区域
    private void initDocLayout() {
        mIdList.add(R.id.live_portrait_info_document);
        mTagList.add(mDocTag);
        mDocTag.setVisibility(View.VISIBLE);
        mDocLayout = new LiveDocComponent(this);
        mLiveInfoList.add(mDocLayout);
        // 设置文档区域可滑动
        mDocLayout.setDocScrollable(true);
    }
    //********************************* 工具方法 ***************************************/

    // 隐藏输入法
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mLiveTopLayout.getWindowToken(), 0);
        }
    }

}


