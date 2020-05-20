package com.bokecc.dwlivedemo.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.live.chat.util.DensityUtil;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.chat.ReplayChatComponent;
import com.bokecc.livemodule.replay.doc.ReplayDocComponent;
import com.bokecc.livemodule.replay.intro.ReplayIntroComponent;
import com.bokecc.livemodule.replay.qa.ReplayQAComponent;
import com.bokecc.livemodule.replay.room.ReplayRoomLayout;
import com.bokecc.livemodule.replay.video.ReplayVideoView;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.OnMarqueeImgFailListener;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.pojo.Marquee;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.widget.MarqueeView;

import java.util.ArrayList;
import java.util.List;

import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_IMAGE_COMPONENT;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_ULR_COMPONET;

import static android.view.View.VISIBLE;

/**
 * 回放播放页（默认文档大屏，视频小屏，可手动切换）
 */
public class ReplayPlayActivity extends BaseActivity {
    private static final String TAG = "ReplayPlayActivity";
    View mRoot;

    LinearLayout mReplayMsgLayout;

    RelativeLayout mReplayVideoContainer;
    // 回放视频View
    ReplayVideoView mReplayVideoView;

    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mReplayFloatingView;
    // 回放房间组件
    ReplayRoomLayout mReplayRoomLayout;
    // 跑马灯组件
    MarqueeView mMarqueeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestFullScreenFeature();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_play);
        initViews();
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                showFloatingDocLayout();
            }
        }, 200);
        mReplayVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReplayVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReplayFloatingView.dismiss();
        mReplayVideoView.destroy();
    }

    @Override
    public void onBackPressed() {
        ELog.d(TAG, "onBackPressed()");
        if (!isPortrait()) {
            quitFullScreen();
            return;
        }
        if (mExitPopupWindow != null) {
            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
            mExitPopupWindow.show(mRoot);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 横屏隐藏状态栏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(true));
        } else {
            getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
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
        mReplayVideoContainer = findViewById(R.id.rl_video_container);
        mReplayVideoView = findViewById(R.id.replay_video_view);
        mReplayRoomLayout = findViewById(R.id.replay_room_layout);
        mReplayRoomLayout.setVideoView(mReplayVideoView);


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
        // 判断当前直播间模版是否有"文档"功能
        if (dwReplayCoreHandler.hasPdfView()) {
            initDocLayout();
            ELog.d(TAG, "initDocLayout");
        }
        // 判断当前直播间模版是否有"聊天"功能
        if (dwReplayCoreHandler.hasChatView()) {
            initChatLayout();
            ELog.d(TAG, "initChatLayout");
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwReplayCoreHandler.hasQaView()) {
            initQaLayout();
            ELog.d(TAG, "initQaLayout");
        }
        // 直播间简介
        initIntroLayout();

        if (DWLiveReplay.getInstance()!=null&&DWLiveReplay.getInstance().getRoomInfo()!=null){
            if (DWLiveReplay.getInstance().getRoomInfo().getOpenMarquee()==1){
                //设置跑马灯
                mMarqueeView = findViewById(R.id.marquee_view);
                mMarqueeView.setVisibility(VISIBLE);
                setMarquee((Marquee) getIntent().getSerializableExtra("marquee"));
            }
        }
    }

    public void setMarquee(final Marquee marquee) {
        final ViewGroup parent = (ViewGroup) mMarqueeView.getParent();
        if (parent.getWidth()!=0&&parent.getHeight()!=0){
            if (marquee != null && marquee.getAction() != null) {
                if (marquee.getType().equals("text")) {
                    mMarqueeView.setTextContent(marquee.getText().getContent());
                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(this,marquee.getText().getFont_size()));
                    mMarqueeView.setType(1);
                } else {
                    mMarqueeView.setMarqueeImage(this, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                    mMarqueeView.setType(2);
                }
                mMarqueeView.setMarquee(marquee,parent.getHeight(),parent.getWidth());
                mMarqueeView.setOnMarqueeImgFailListener(new OnMarqueeImgFailListener() {
                    @Override
                    public void onLoadMarqueeImgFail() {
                        //跑马灯加载失败
                        toastOnUiThread("跑马灯加载失败");
                    }
                });
                mMarqueeView.start();
            }
        }else{
            parent.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= 16) {
                                parent.getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                            }
                            else {
                                parent.getViewTreeObserver()
                                        .removeGlobalOnLayoutListener(this);
                            }
                            int width = parent.getWidth();// 获取宽度
                            int height = parent.getHeight();// 获取高度
                            if (marquee != null && marquee.getAction() != null) {
                                if (marquee.getType().equals("text")) {
                                    mMarqueeView.setTextContent(marquee.getText().getContent());
                                    mMarqueeView.setTextColor(marquee.getText().getColor().replace("0x", "#"));
                                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(ReplayPlayActivity.this,marquee.getText().getFont_size()));
                                    mMarqueeView.setType(1);
                                } else {
                                    mMarqueeView.setMarqueeImage(ReplayPlayActivity.this, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
                                    mMarqueeView.setType(2);
                                }
                                mMarqueeView.setMarquee(marquee,height,width);
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

    /********************************* 重要组件相关 ***************************************/

    // 简介组件
    ReplayIntroComponent mIntroComponent;

    // 问答组件
    ReplayQAComponent mQaLayout;

    // 聊天组件
    ReplayChatComponent mChatLayout;

    // 文档组件
    ReplayDocComponent mDocLayout;

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(VISIBLE);
        mChatLayout = new ReplayChatComponent(this);
        mChatLayout.setOnChatComponentClickListener(new OnChatComponentClickListener() {
            @Override
            public void onClickChatComponent(Bundle bundle) {
                if (bundle == null) return;
                String type = bundle.getString("type");
                if (CONTENT_IMAGE_COMPONENT.equals(type)) {
                    Intent intent = new Intent(ReplayPlayActivity.this, ImageDetailsActivity.class);
                    intent.putExtra("imageUrl", bundle.getString("url"));
                    startActivity(intent);
                } else if (CONTENT_ULR_COMPONET.equals(type)) {
                    Uri uri = Uri.parse(bundle.getString("url"));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
        mLiveInfoList.add(mChatLayout);
        if (mChatLayout != null) {
            mReplayRoomLayout.setSeekListener(mChatLayout);
        }
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(VISIBLE);
        mQaLayout = new ReplayQAComponent(this);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        ELog.d(TAG, "initIntroLayout");
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(VISIBLE);
        mIntroComponent = new ReplayIntroComponent(this);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化文档布局区域
    private void initDocLayout() {
        mDocLayout = new ReplayDocComponent(this);
        mDocLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mReplayFloatingView.addView(mDocLayout);
    }

    // 展示文档悬浮窗布局
    private void showFloatingDocLayout() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler == null) {
            return;
        }
        ELog.d(TAG, "showFloatingDocLayout() hasPdfView:" + dwReplayCoreHandler.hasPdfView());
        // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
        if (dwReplayCoreHandler.hasPdfView()) {
            if (!mReplayFloatingView.isShowing()) {
                mReplayFloatingView.show(mRoot);
            }
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

    boolean isVideoMain = true;

    private ReplayRoomLayout.ReplayRoomStatusListener roomStatusListener = new ReplayRoomLayout.ReplayRoomStatusListener() {

        @Override
        public void switchVideoDoc() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isVideoMain) {
                        // 缓存视频的切换前的画面
                        mReplayVideoContainer.removeAllViews();
                        mReplayFloatingView.removeAllView();
                        mReplayFloatingView.addView(mReplayVideoView);
                        mReplayVideoContainer.addView(mDocLayout);
                        isVideoMain = false;
                        mReplayRoomLayout.setVideoDocSwitchText("切换视频");
                        mDocLayout.setDocScrollable(true);//webview可切换
                    } else {
                        // 缓存视频的切换前的画面
                        mReplayVideoContainer.removeAllViews();
                        mReplayFloatingView.removeAllView();
                        ViewGroup.LayoutParams lp = mDocLayout.getLayoutParams();
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mDocLayout.setLayoutParams(lp);
                        mReplayFloatingView.addView(mDocLayout);
                        mReplayVideoContainer.addView(mReplayVideoView);
                        isVideoMain = true;
                        mReplayRoomLayout.setVideoDocSwitchText("切换文档");
                        mDocLayout.setDocScrollable(false);//webview不可切换
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
                    getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(true));
                }
            });
        }


        @Override
        public void onClickDocScaleType(int type) {
            if (mDocLayout != null) {
                mDocLayout.setScaleType(type);
            }
        }
    };

    //---------------------------------- 全屏相关逻辑 --------------------------------------------/

    // 退出全屏
    private void quitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mReplayMsgLayout.setVisibility(VISIBLE);
        mReplayRoomLayout.quitFullScreen();

        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility(false));
    }

    //---------------------------------- 退出相关逻辑 --------------------------------------------/

    ExitPopupWindow mExitPopupWindow;

    ExitPopupWindow.ConfirmExitRoomListener confirmExitRoomListener = new ExitPopupWindow.ConfirmExitRoomListener() {
        @Override
        public void onConfirmExitRoom() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mChatLayout != null) {
                        mChatLayout.release();
                    }
                    if (mChatLayout != null) {
                        mReplayRoomLayout.release();
                    }

                    mExitPopupWindow.dismiss();
                    finish();
                }
            });
        }
    };

}
