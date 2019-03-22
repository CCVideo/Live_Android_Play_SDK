package com.bokecc.livemodule.live.video;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveVideoListener;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLivePlayer;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.logging.LogHelper;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * CC 直播视频展示控件
 */
public class LiveVideoView extends RelativeLayout implements DWLiveVideoListener {

    private Context mContext;

    private WindowManager wm;

    View mRootView;

    TextureView mVideoContainer;

    TextView mVideoNoplayTip;

    ProgressBar mVideoProgressBar;

    DWLivePlayer player;

    Surface surface;

    public LiveVideoView(Context context) {
        super(context);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public LiveVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public LiveVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    private void inflateViews() {
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, this);
        mVideoContainer = mRootView.findViewById(R.id.live_video_container);
        mVideoNoplayTip = mRootView.findViewById(R.id.tv_video_no_play_tip);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mVideoContainer.setSurfaceTextureListener(surfaceTextureListener);
        player = new DWLivePlayer(mContext);
        player.setOnPreparedListener(onPreparedListener);
        player.setOnInfoListener(onInfoListener);
        player.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setPlayer(player);
            dwLiveCoreHandler.setDwLiveVideoListener(this);
        }
    }

    // 视频播放控件进入连麦模式
    public void enterRtcMode(boolean isVideoRtc) {
        // 如果是视频连麦，则将播放器停止
        if (isVideoRtc) {
            player.pause();
            player.stop();
            setVisibility(INVISIBLE);
        } else {
            // 如果是音频连麦，只需将播放器的音频关闭掉
            player.setVolume(0f, 0f);
        }
    }


    // 视频播放控件退出连麦模式
    public void exitRtcMode() {
        try {
            DWLive.getInstance().restartVideo(surface);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DWLiveException e) {
            e.printStackTrace();
        }
        setVisibility(VISIBLE);
    }

    /**
     * 添加此字段的意义在于：
     * 部分手机HOME到桌面回来时不触发onSurfaceTextureAvailable，需要由onResume来触发一次调用逻辑。
     * 此字段在调用开始播放的时候使用，后面无论播放是否开始都需要在合适的时机恢复为false.
     */
    boolean hasCallStartPlay = false;

    /**
     * 开始播放
     */
    public synchronized void start() {
        if (hasCallStartPlay) {
            return;
        }
        LogHelper.getInstance().writeLog("LiveVideoView Call DwLiveCoreHandler start");
        hasCallStartPlay = true;
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.start(surface);
            if (mVideoProgressBar != null) {
                mVideoProgressBar.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        LogHelper.getInstance().writeLog("LiveVideoView Call DwLiveCoreHandler Stop");
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.stop();
        }
    }

    public void destroy() {
        LogHelper.getInstance().writeLog("LiveVideoView Call DwLiveCoreHandler destroy");
        if (player != null) {
            player.pause();
            player.stop();
            player.release();
        }

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.destroy();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);
            if (player.isPlaying()) {
                player.setSurface(surface);
            } else {
                if (hasCallStartPlay) {
                    return;
                }
                start();
                hasCallStartPlay = true;
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mVideoNoplayTip.post(new Runnable() {
                @Override
                public void run() {
                    hasCallStartPlay = false;  // 准备正常播放了，将字段回归为false;
                    player.start();
                    mVideoNoplayTip.setVisibility(GONE);
                    mVideoProgressBar.setVisibility(GONE);
                }
            });
        }
    };

    IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                // 缓冲开始
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mVideoProgressBar.setVisibility(VISIBLE);
                    break;
                // 缓冲结束
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mVideoProgressBar.setVisibility(GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mVideoProgressBar.setVisibility(GONE);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVideoContainer.setLayoutParams(getVideoSizeParams());
    }

    IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            if (width == 0 || height == 0) {
                return;
            }
            mVideoContainer.setLayoutParams(getVideoSizeParams());
        }
    };

    // 视频等比缩放
    private RelativeLayout.LayoutParams getVideoSizeParams() {

        int width = wm.getDefaultDisplay().getWidth();
        int height;
        if(isPortrait()) {
            height = wm.getDefaultDisplay().getHeight() / 3;  //TODO 可以根据当前布局方式更改此参数
        } else {
            height = wm.getDefaultDisplay().getHeight();
        }


        int vWidth = player.getVideoWidth();
        int vHeight = player.getVideoHeight();

        if (vWidth == 0) {
            vWidth = 600;
        }
        if (vHeight == 0) {
            vHeight = 400;
        }

        if (vWidth > width || vHeight > height) {
            float wRatio = (float) vWidth / (float) width;
            float hRatio = (float) vHeight / (float) height;
            float ratio = Math.max(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth / ratio);
            height = (int) Math.ceil((float) vHeight / ratio);
        } else {
            float wRatio = (float) width / (float) vWidth;
            float hRatio = (float) height / (float) vHeight;
            float ratio = Math.min(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth * ratio);
            height = (int) Math.ceil((float) vHeight * ratio);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        return params;
    }

    // 判断当前屏幕朝向是否为竖屏
    private boolean isPortrait() {
        int mOrientation = mContext.getResources().getConfiguration().orientation;
        return mOrientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    //------------------------------------- SDK 回调相关 ---------------------------------------
    // 由 DWLiveListener(DWLiveCoreHandler) --> DWLiveVideoListener(LiveVideoView)

    @Override
    public void onStreamEnd(boolean isNormal) {
        mRootView.post(new Runnable() {
            @Override
            public void run() {
                player.pause();
                player.stop();
                player.reset();
                mVideoProgressBar.setVisibility(View.GONE);
                mVideoNoplayTip.setVisibility(View.VISIBLE);
                mVideoNoplayTip.setText("直播已结束");

            }
        });
    }

    /**
     * 播放状态
     *
     * @param status 包括PLAYING, PREPARING共2种状态
     */
    @Override
    public void onLiveStatus(final DWLive.PlayStatus status) {
        mRootView.post(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case PLAYING:
                        mVideoNoplayTip.setVisibility(View.GONE);
                        break;
                    case PREPARING:
                        mVideoProgressBar.setVisibility(GONE);
                        mVideoNoplayTip.setVisibility(View.VISIBLE);
                        mVideoNoplayTip.setText("直播未开始");
                        // 如果判断当前直播未开始，也将字段回归为false;
                        hasCallStartPlay = false;
                        break;
                }

            }
        });
    }

    /*
     * 禁播
     *
     * @param reason
     */
    @Override
    public void onBanStream(String reason) {
        mRootView.post(new Runnable() {
            @Override
            public void run() {
                // 播放器停止播放
                if (player != null) {
                    player.stop();
                }
                // 隐藏加载控件
                if (mVideoProgressBar != null) {
                    mVideoProgressBar.setVisibility(GONE);
                }
                // 展示'直播间已封禁'的标识
                if (mVideoNoplayTip != null) {
                    mVideoNoplayTip.setVisibility(View.VISIBLE);
                    mVideoNoplayTip.setText("直播间已封禁");
                }
            }
        });
    }

    /**
     * 解禁
     */
    @Override
    public void onUnbanStream() {
        if (surface != null) {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null) {
                dwLiveCoreHandler.start(surface);
            }
        }
    }
}
