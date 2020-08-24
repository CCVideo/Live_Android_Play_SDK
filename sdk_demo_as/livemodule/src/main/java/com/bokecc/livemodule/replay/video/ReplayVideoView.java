package com.bokecc.livemodule.replay.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.view.ResizeTextureView;
import com.bokecc.livemodule.view.VideoLoadingView;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * CC 回放视频展示控件
 */
public class ReplayVideoView extends RelativeLayout {
    private static final String TAG = "ReplayVideoView";
    private Context mContext;
    private ResizeTextureView mTextureView;
    private TextView mVideoNoPlayTip;
    private VideoLoadingView mVideoProgressBar;
    private DWReplayPlayer player;
    private Handler handler = new Handler();
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    public ReplayVideoView(Context context) {
        super(context);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public ReplayVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public ReplayVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    private void inflateViews() {
        View mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, this);
        mTextureView = mRootView.findViewById(R.id.live_video_container);
        mVideoNoPlayTip = mRootView.findViewById(R.id.tv_video_no_play_tip);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        player = new DWReplayPlayer(mContext);
        player.setOnPreparedListener(preparedListener);
        player.setOnInfoListener(infoListener);
        player.setOnBufferingUpdateListener(bufferingUpdateListener);
        player.setOnErrorListener(errorListener);
        player.setOnCompletionListener(completionListener);
        player.setOnVideoSizeChangedListener(videoSizeChangedListener);
        player.setSpeedListener(replaySpeedListener);
        player.setBufferTimeout(20);
        player.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer mp) {
            }
        });
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.setPlayer(player);
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.start();
        }
    }


    public void pause() {
        DWReplayCoreHandler handler = DWReplayCoreHandler.getInstance();
        if (handler != null) {
            handler.pause();
        }
    }


    public void destroy() {
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.destroy();
        }
    }


    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {


        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mSurfaceTexture != null) {
                //如果是从后台切换回前台的话
                if (isNeedUpdateSurface){
                    if (mSurfaceTexture.hashCode()==surfaceTexture.hashCode()){
                        mTextureView.setSurfaceTexture(mSurfaceTexture);
                    }else{
                        mSurfaceTexture = surfaceTexture;
                        mSurface = new Surface(surfaceTexture);
                        player.updateSurface(mSurface);
                    }
                }else{
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                }
                isNeedUpdateSurface = false;
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


    public void showProgress() {
        if (mVideoProgressBar != null) {
            mVideoProgressBar.setVisibility(VISIBLE);
        }
    }


    public void dismissProgress() {
        if (mVideoProgressBar != null) {
            mVideoProgressBar.setVisibility(GONE);
        }
    }

    /******************************************* 播放器相关监听 ***********************************/

    IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mVideoNoPlayTip.post(new Runnable() {
                @Override
                public void run() {
                    DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
                    if (dwReplayCoreHandler != null) {
                        dwReplayCoreHandler.replayVideoPrepared();
                    }


                }
            });
        }
    };

    IMediaPlayer.OnInfoListener infoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();

            switch (what) {
                // 缓冲开始
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    showProgress();
                    if (dwReplayCoreHandler != null) {
                        dwReplayCoreHandler.bufferStart();
                    }
                    break;
                // 缓冲结束
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    dismissProgress();
                    if (dwReplayCoreHandler != null) {
                        dwReplayCoreHandler.bufferEnd();
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mVideoNoPlayTip.setVisibility(GONE);
                    dismissProgress();
                    if (dwReplayCoreHandler != null) {
                        dwReplayCoreHandler.onRenderStart();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    IMediaPlayer.OnBufferingUpdateListener bufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.updateBufferPercent(percent);
            }
        }
    };
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
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            dismissProgress();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.playError(what);
            }
            return false;
        }
    };

    IMediaPlayer.OnCompletionListener completionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            dismissProgress();
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            if (dwReplayCoreHandler != null) {
                dwReplayCoreHandler.onPlayComplete();


            }
        }
    };
    DWReplayPlayer.ReplaySpeedListener replaySpeedListener = new DWReplayPlayer.ReplaySpeedListener() {
        @Override
        public void onBufferSpeed(float speed) {
            mVideoProgressBar.setSpeed(speed);
        }
    };
    /**
     * 记录用户是否切换到后台 因为部分手机切换到后台再切回到前台会出现视频帧卡住问题
     */
    private boolean isNeedUpdateSurface;
    public void onPause(){
        isNeedUpdateSurface = true;
    }
    public void setShowSpeed(boolean showSpeed) {
        mVideoProgressBar.showSpeeed(showSpeed);
    }
}
