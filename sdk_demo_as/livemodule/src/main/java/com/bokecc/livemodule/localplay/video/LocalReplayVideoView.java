package com.bokecc.livemodule.localplay.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.view.ResizeTextureView;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * CC 回放视频展示控件
 */
public class LocalReplayVideoView extends RelativeLayout {

    Context mContext;

    View mRootView;

    ResizeTextureView mTextureView;

    TextView mVideoNoplayTip;

    ProgressBar mVideoProgressBar;

    DWReplayPlayer player;

    String playPath;

    public LocalReplayVideoView(Context context) {
        super(context);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public LocalReplayVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public LocalReplayVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    private void inflateViews() {
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, this);
        mTextureView = mRootView.findViewById(R.id.live_video_container);
        mVideoNoplayTip = mRootView.findViewById(R.id.tv_video_no_play_tip);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        player = new DWReplayPlayer(getContext());
        player.setOnPreparedListener(preparedListener);
        player.setOnVideoSizeChangedListener(videoSizeChangedListener);
        player.setOnInfoListener(infoListener);
        player.setOnErrorListener(errorListener);
        player.setOnCompletionListener(completionListener);
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.setPlayer(player);
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener videoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            if (width != 0 && height != 0) {
                mTextureView.setVideoSize(width, height);
            }
        }
    };
    IMediaPlayer.OnErrorListener errorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            DWLocalReplayCoreHandler dwReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.playError(what);
            }
            return false;
        }
    };

    IMediaPlayer.OnCompletionListener completionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            DWLocalReplayCoreHandler dwReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.onPlayComplete();
            }
        }
    };

    /**
     * 开始播放
     */
    public void start() {
        if (player.getPlayerState() == DWReplayPlayer.State.PAUSED) {
            return;
        }
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.start();
        }
    }

    long currentPosition;
    float currentSpeed = 1.0f;

    /**
     * 停止播放
     */
    public void stop() {
        if (player != null) {
            player.pause();
            if (player.getCurrentPosition() != 0) {
                currentSpeed = player.getSpeed(0);
                currentPosition = player.getCurrentPosition();
            }
        }
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.stop();
        }
    }

    public void destroy() {
        if (player != null) {
            player.pause();
            player.stop();
        }
        DWLocalReplayCoreHandler dwLocalReplayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (dwLocalReplayCoreHandler != null) {
            dwLocalReplayCoreHandler.destroy();
        }
    }

    Bitmap tempBitmap;

    // 缓存视频的切换前的画面
    public void cacheScreenBitmap() {
        tempBitmap = mTextureView.getBitmap();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mSurfaceTexture != null) {
                mTextureView.setSurfaceTexture(mSurfaceTexture);
            } else {
                mSurfaceTexture = surfaceTexture;
                mSurface = new Surface(surfaceTexture);
                player.updateSurface(mSurface);
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


    /******************************************* 播放器相关监听 ***********************************/

    IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mVideoNoplayTip.post(new Runnable() {
                @Override
                public void run() {
                    player.start();
                    if (currentPosition > 0) {
                        player.seekTo(currentPosition);
                        player.setSpeed(currentSpeed);
                    }
                    mVideoNoplayTip.setVisibility(GONE);

                    DWLocalReplayCoreHandler handler = DWLocalReplayCoreHandler.getInstance();
                    if (handler != null) {
                        handler.replayVideoPrepared();
                    }
                }
            });
        }
    };

    IMediaPlayer.OnInfoListener infoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                // 缓冲开始
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mVideoProgressBar.setVisibility(VISIBLE);
                    break;
                // 缓冲结束
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mVideoProgressBar.setVisibility(GONE);
                    break;
                default:
                    break;
            }
            return false;
        }
    };


    /**
     * 设置播放路径（即文件名：*****.ccr）
     *
     * @param fileName 播放路径
     */
    public void setPlayPath(String fileName) {
        this.playPath = fileName;
        DWLocalReplayCoreHandler.getInstance().setPlayPath(playPath);
    }
}
