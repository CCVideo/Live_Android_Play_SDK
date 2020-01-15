package com.bokecc.dwlivedemo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.localplay.chat.LocalReplayChatComponent;
import com.bokecc.livemodule.localplay.doc.LocalReplayDocComponent;
import com.bokecc.livemodule.localplay.intro.LocalReplayIntroComponent;
import com.bokecc.livemodule.localplay.qa.LocalReplayQAComponent;
import com.bokecc.livemodule.localplay.room.LocalReplayRoomLayout;
import com.bokecc.livemodule.localplay.video.LocalReplayVideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_IMAGE_COMPONENT;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_ULR_COMPONET;

/**
 * 离线回放播放页
 */
public class LocalReplayPlayActivity extends BaseActivity implements DWLocalReplayCoreHandler.LocalTemplateUpdateListener {

    public static String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/CCDownload";

    private View mRoot;
    private String mPlayPath;  // CCR文件名
    private LocalReplayVideoView mReplayVideoView;
    private RelativeLayout mReplayVideoContainer;
    private LocalReplayRoomLayout mReplayRoomLayout;
    private LinearLayout mReplayMsgLayout;

    // 悬浮弹窗（用于展示文档和视频）
    FloatingPopupWindow mLocalReplayFloatingView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.activity_local_replay_play);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "CCR文件名为空，播放失败！", Toast.LENGTH_LONG).show();
            return;
        }

        File oriFile = new File(DOWNLOAD_DIR, fileName);
        mPlayPath = getUnzipDir(oriFile);

        DWLocalReplayCoreHandler handler = DWLocalReplayCoreHandler.getInstance();
        handler.setLocalTemplateUpdateListener(this);

        initViews();
    }

    public static String getUnzipDir(File oriFile) {

        String fileName = oriFile.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(oriFile.getParent());
        sb.append("/");
        int index = fileName.indexOf(".");
        if (index == -1) {
            sb.append(fileName);
        } else {
            sb.append(fileName.substring(0, index));
        }

        return sb.toString();
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

    PagerAdapter adapter;

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        adapter = new PagerAdapter() {
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

    /********************************* 重要组件相关 ***************************************/

    //因为息屏会重新触发一次onLocalTemplateUpdate回调
    private boolean isComponentsInit = false;

    /**
     * 根据直播间模版初始化相关组件
     */
    private void initComponents() {

        if (isComponentsInit) {
            return;
        }
        isComponentsInit = true;

        mLiveInfoList.clear();
        mIdList.clear();

        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler == null) {
            return;
        }
        // 判断当前直播间模版是否有"文档"功能
        if (dwLocalReplayCoreHandler.hasPdfView()) {
            initDocLayout();
        }
        // 判断当前直播间模版是否有"聊天"功能
        if (dwLocalReplayCoreHandler.hasChatView()) {
            initChatLayout();
        }
        // 判断当前直播间模版是否有"问答"功能
        if (dwLocalReplayCoreHandler.hasQaView()) {
            initQaLayout();
        }
        // 直播间简介
        initIntroLayout();
        initViewPager();
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                showFloatingDocLayout();
            }
        }, 200);
    }


    // 简介组件
    LocalReplayIntroComponent mIntroComponent;
    // 问答组件
    LocalReplayQAComponent mQaLayout;
    // 聊天组件
    LocalReplayChatComponent mChatLayout;
    // 文档组件
    LocalReplayDocComponent mDocLayout;

    // 初始化聊天布局区域
    private void initChatLayout() {
        mIdList.add(R.id.live_portrait_info_chat);
        mTagList.add(mChatTag);
        mChatTag.setVisibility(View.VISIBLE);
        mLiveInfoList.add(mChatLayout);
    }

    // 初始化问答布局区域
    private void initQaLayout() {
        mIdList.add(R.id.live_portrait_info_qa);
        mTagList.add(mQaTag);
        mQaTag.setVisibility(View.VISIBLE);
        mLiveInfoList.add(mQaLayout);
    }

    // 初始化简介布局区域
    private void initIntroLayout() {
        mIdList.add(R.id.live_portrait_info_intro);
        mTagList.add(mIntroTag);
        mIntroTag.setVisibility(View.VISIBLE);
        mLiveInfoList.add(mIntroComponent);
    }

    // 初始化文档布局区域
    private void initDocLayout() {
        mLocalReplayFloatingView.removeAllView();
        if (isVideoMain) {
            mLocalReplayFloatingView.addView(mDocLayout);
        } else {
            mLocalReplayFloatingView.addView(mReplayVideoView);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                mReplayVideoView.start();
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
        mLocalReplayFloatingView.dismiss();
        mReplayVideoView.destroy();

    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        mReplayVideoView = findViewById(R.id.replay_video_view);
        mReplayVideoContainer = findViewById(R.id.rl_video_container);
        mReplayVideoView.setPlayPath(mPlayPath);
        mReplayRoomLayout = findViewById(R.id.replay_room_layout);
        mReplayRoomLayout.setReplayRoomStatusListener(roomStatusListener);
        mReplayMsgLayout = findViewById(R.id.ll_pc_replay_msg_layout);
        mViewPager = findViewById(R.id.live_portrait_container_viewpager);
        mRadioGroup = findViewById(R.id.rg_infos_tag);
        mIntroTag = findViewById(R.id.live_portrait_info_intro);
        mQaTag = findViewById(R.id.live_portrait_info_qa);
        mChatTag = findViewById(R.id.live_portrait_info_chat);
        mDocTag = findViewById(R.id.live_portrait_info_document);

        mLocalReplayFloatingView = new FloatingPopupWindow(this);
        //退出确认弹框
        mExitPopupWindow = new ExitPopupWindow(this);

        mChatLayout = new LocalReplayChatComponent(this);
        mQaLayout = new LocalReplayQAComponent(this);
        mIntroComponent = new LocalReplayIntroComponent(this);
        mDocLayout = new LocalReplayDocComponent(this);

        mChatLayout.setOnChatComponentClickListener(new OnChatComponentClickListener() {
            @Override
            public void onClickChatComponent(Bundle bundle) {
                if (bundle == null) return;

                String type = bundle.getString("type");
                if (CONTENT_IMAGE_COMPONENT.equals(type)) {
                    Intent intent = new Intent(LocalReplayPlayActivity.this, ImageDetailsActivity.class);
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


    // 展示文档悬浮窗布局
    private void showFloatingDocLayout() {
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler == null) {
            return;
        }
        // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
        if (dwLocalReplayCoreHandler.hasPdfView()) {
            if (!mLocalReplayFloatingView.isShowing()) {
                mLocalReplayFloatingView.show(mRoot);
            }
        }
    }


    /**************************************  Room 状态回调监听 *************************************/

    boolean isVideoMain = true;

    private LocalReplayRoomLayout.LocalReplayRoomStatusListener roomStatusListener = new LocalReplayRoomLayout.LocalReplayRoomStatusListener() {

        @Override
        public void switchVideoDoc() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isVideoMain) {
                        // 缓存视频的切换前的画面
                        mReplayVideoContainer.removeAllViews();
                        mLocalReplayFloatingView.removeAllView();
                        mLocalReplayFloatingView.addView(mReplayVideoView);
                        mReplayVideoContainer.addView(mDocLayout);
                        isVideoMain = false;
                        mReplayRoomLayout.setVideoDocSwitchText("切换视频");
                    } else {
                        // 缓存视频的切换前的画面
                        mReplayVideoContainer.removeAllViews();
                        mLocalReplayFloatingView.removeAllView();
                        mLocalReplayFloatingView.addView(mDocLayout);
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
                    mChatLayout.stopTimerTask();
                    mReplayRoomLayout.stopTimerTask();
                    mExitPopupWindow.dismiss();
                    finish();
                }
            });
        }
    };

    /**
     * 模板信息获取监听器，当本地回放模板信获取成功后该方法将会回调
     */
    @Override
    public void onLocalTemplateUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initComponents();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            quitFullScreen();
            return;
        }
        finish();
//        if (mExitPopupWindow != null) {
//            mExitPopupWindow.setConfirmExitRoomListener(confirmExitRoomListener);
//            mExitPopupWindow.show(mRoot);
//        }
    }


}
