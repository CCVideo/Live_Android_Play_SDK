package com.bokecc.dwlivedemo.activity;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BaseActivity;
import com.bokecc.dwlivedemo.popup.ExitPopupWindow;
import com.bokecc.dwlivedemo.popup.FloatingPopupWindow;
import com.bokecc.livemodule.live.chat.OnChatComponentClickListener;
import com.bokecc.livemodule.live.chat.util.DensityUtil;
import com.bokecc.livemodule.live.room.LiveRoomLayout;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.chat.ReplayChatComponent;
import com.bokecc.livemodule.replay.doc.ReplayDocComponent;
import com.bokecc.livemodule.replay.intro.ReplayIntroComponent;
import com.bokecc.livemodule.replay.qa.ReplayQAComponent;
import com.bokecc.livemodule.replay.room.ReplayRoomLayout;
import com.bokecc.livemodule.replay.video.ReplayVideoView;
import com.bokecc.sdk.mobile.live.OnMarqueeImgFailListener;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.pojo.Marquee;
import com.bokecc.sdk.mobile.live.replay.ReplayLineSwitchListener;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.entity.ReplayLineParams;
import com.bokecc.sdk.mobile.live.replay.config.ReplayLineConfig;
import com.bokecc.sdk.mobile.live.widget.MarqueeView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_IMAGE_COMPONENT;
import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.CONTENT_ULR_COMPONET;

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

    private NotificationReceiver mNotificationReceiver;
    private boolean isVideo = false;
    private final String CHANNEL_ID = "HD_SDK_CHANNEL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestFullScreenFeature();
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_play);
        initViews();
        showFloatingDocLayout();


        //DWLiveReplay.getInstance().setLastPosition(30000);
        mReplayVideoView.start();
        initViewPager();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReplayFloatingView.dismiss();
        mReplayVideoView.destroy();
        NotificationManager notificationManager = (NotificationManager) ReplayPlayActivity.this
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
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
        //调整窗口的位置
        if (mReplayFloatingView != null) {
            mReplayFloatingView.onConfigurationChanged(newConfig.orientation);
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
        mReplayFloatingView.setFloatDismissListener(floatDismissListener);
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

        if (DWLiveReplay.getInstance() != null && DWLiveReplay.getInstance().getRoomInfo() != null) {
            if (DWLiveReplay.getInstance().getRoomInfo().getOpenMarquee() == 1) {
                //设置跑马灯
                mMarqueeView = findViewById(R.id.marquee_view);
                mMarqueeView.setVisibility(VISIBLE);
                setMarquee((Marquee) getIntent().getSerializableExtra("marquee"));
            }
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
                                    mMarqueeView.setTextFontSize((int) DensityUtil.sp2px(ReplayPlayActivity.this, marquee.getText().getFont_size()));
                                    mMarqueeView.setType(1);
                                } else {
                                    mMarqueeView.setMarqueeImage(ReplayPlayActivity.this, marquee.getImage().getImage_url(), marquee.getImage().getWidth(), marquee.getImage().getHeight());
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
    private FloatingPopupWindow.FloatDismissListener floatDismissListener = new FloatingPopupWindow.FloatDismissListener() {
        @Override
        public void dismiss() {
            if (mReplayRoomLayout.viewState == LiveRoomLayout.State.VIDEO) {
                mReplayRoomLayout.setVideoDocSwitchText(LiveRoomLayout.State.OPEN_DOC);
            } else if (mReplayRoomLayout.viewState == LiveRoomLayout.State.DOC) {
                mReplayRoomLayout.setVideoDocSwitchText(LiveRoomLayout.State.OPEN_VIDEO);
            }
        }
    };
    private ReplayRoomLayout.ReplayRoomStatusListener roomStatusListener = new ReplayRoomLayout.ReplayRoomStatusListener() {

        @Override
        public void switchVideoDoc(LiveRoomLayout.State state) {

            if (state == LiveRoomLayout.State.VIDEO) {
                //如果当前小窗口开启并且大窗口是视频 将大窗口切换到文档
                switchView(true);
            } else if (state == LiveRoomLayout.State.DOC) {
                switchView(false);
            } else if (state == LiveRoomLayout.State.OPEN_DOC) {
                mReplayFloatingView.show(mRoot);
                if (mDocLayout.getParent() != null)
                    ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
                mReplayFloatingView.addView(mDocLayout);
            } else if (state == LiveRoomLayout.State.OPEN_VIDEO) {
                mReplayFloatingView.show(mRoot);
                if (mReplayVideoView.getParent() != null)
                    ((ViewGroup) mReplayVideoView.getParent()).removeView(mReplayVideoView);
                mReplayFloatingView.addView(mReplayVideoView);
            }
        }

        public void switchView(boolean isVideoMain) {
            if (mReplayVideoView.getParent() != null)
                ((ViewGroup) mReplayVideoView.getParent()).removeView(mReplayVideoView);
            if (mDocLayout.getParent() != null)
                ((ViewGroup) mDocLayout.getParent()).removeView(mDocLayout);
            if (isVideoMain) {
                // 缓存视频的切换前的画面
                ViewGroup.LayoutParams lp = mDocLayout.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mDocLayout.setLayoutParams(lp);
                mReplayFloatingView.addView(mDocLayout);
                mReplayVideoContainer.addView(mReplayVideoView);
                mDocLayout.setDocScrollable(false);//webview不可切换
            } else {
                // 缓存视频的切换前的画面
                mReplayFloatingView.addView(mReplayVideoView);
                mReplayVideoContainer.addView(mDocLayout);
                mDocLayout.setDocScrollable(true);//webview可切换
            }
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

        @Override
        public void seek(int max, int progress, float move, boolean isSeek, float xVelocity) {
            DWReplayPlayer player = DWReplayCoreHandler.getInstance().getPlayer();
            if (progress + move < 0) {
                mReplayRoomLayout.mPlaySeekBar.setProgress(0);
            } else if (progress + move >= max) {
                mReplayRoomLayout.mPlaySeekBar.setProgress(max);
            } else if (progress + move < max) {
                mReplayRoomLayout.mPlaySeekBar.setProgress((int) (progress + ((xVelocity * 10))));
            }
//            else if (progress + move == max) {
//                mReplayRoomLayout.mPlaySeekBar.setProgress(0);
//            }
            if (isSeek) {
                player.seekTo(mReplayRoomLayout.mPlaySeekBar.getProgress());
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

    public void showAlwaysNotify(int playResId) {
        NotificationReceiver.notifiCallBack = new NotificationReceiver.NotifiCallBack() {

            @Override
            public void clickPlay() {
                mReplayRoomLayout.changePlayerStatus();
                showAlwaysNotify(DWReplayCoreHandler.getInstance().getPlayer().isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play);
            }

            @Override
            public void clickExit() {
                finish();
            }
        };
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(this, ReplayPlayActivity.class));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.item_notification);
        remoteViews.setTextViewText(R.id.id_content, DWLiveReplay.getInstance().getRoomInfo()==null?"直播":DWLiveReplay.getInstance().getRoomInfo().getName());
        remoteViews.setImageViewResource(R.id.id_play_btn, playResId);

        //暂停播放
        Intent pauseAction = new Intent(this, NotificationReceiver.class);
        pauseAction.setAction(NotificationReceiver.ACTION_PLAY_PAUSE);
        PendingIntent pendingPauseAction = PendingIntent.getBroadcast(this, -1,
                pauseAction, PendingIntent.FLAG_UPDATE_CURRENT);


        //结束播放
        Intent destroyAction = new Intent(this, NotificationReceiver.class);
        destroyAction.setAction(NotificationReceiver.ACTION_DESTROY);
        PendingIntent pendingDestroyAction = PendingIntent.getBroadcast(this, -1,
                destroyAction, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.id_play_btn, pendingPauseAction);
        remoteViews.setOnClickPendingIntent(R.id.id_close_play, pendingDestroyAction);

        builder.setCustomContentView(remoteViews);
        createNotificationChannel();
        Notification build = builder.build();
        build.flags = Notification.FLAG_NO_CLEAR;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, build);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //TODO：
            CharSequence name = DWLiveReplay.getInstance().getRoomInfo().getName();
            String description = "";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            //锁屏显示通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) ReplayPlayActivity.this
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        NotificationReceiver.notifiCallBack = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        showAlwaysNotify(DWReplayCoreHandler.getInstance().getPlayer().isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play);
        mReplayVideoView.onPause();
    }
}
