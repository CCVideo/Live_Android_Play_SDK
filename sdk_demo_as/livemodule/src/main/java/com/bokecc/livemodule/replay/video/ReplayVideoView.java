package com.bokecc.livemodule.replay.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.util.AttributeSet;
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
import com.bokecc.sdk.mobile.live.logging.ELog;
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

    private ProgressBar mVideoProgressBar;

    private DWReplayPlayer player;

    private Bitmap tempBitmap;

    private long allTime = 0;

    private int videoWidth;
    private int videoHeight;

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
            dwReplayCoreHandler.start(null);
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


    // 缓存视频的切换前的画面
    public void cacheScreenBitmap() {
        tempBitmap = mTextureView.getBitmap();
    }



    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            // 使用新的surfaceTexture生成surface
            ELog.i(TAG, "onSurfaceTextureAvailable");
            Surface surface = new Surface(surfaceTexture);
            // 正在播放中或者暂停中，只需要将新的surface给player即可
            if (player.isPlaying() || (player.isPlayable() && !TextUtils.isEmpty(player.getDataSource()))) {
                // 尝试绘制之前的画面
                try {
                    if (tempBitmap != null && !tempBitmap.isRecycled() && surface.isValid()) {
                        RectF rectF = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
                        Canvas canvas = surface.lockCanvas(new Rect(0, 0, mTextureView.getWidth(), mTextureView.getHeight()));
                        if (canvas != null) {
                            canvas.drawBitmap(tempBitmap, null, rectF, null);
                            surface.unlockCanvasAndPost(canvas);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            player.updateSurface(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            ELog.i(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            ELog.i(TAG, "onSurfaceTextureDestroyed");
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
                    long duration = player.getDuration();
                    allTime = Math.round((double) duration / 1000) * 1000;
                    mVideoNoPlayTip.setVisibility(GONE);
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
                    ELog.e(TAG, "buffer start");
                    break;
                // 缓冲结束
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    dismissProgress();
                    ELog.e(TAG, "buffer end");
                    if (dwReplayCoreHandler != null) {
                        dwReplayCoreHandler.bufferEnd();
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    dismissProgress();
                    if (dwReplayCoreHandler != null) {
                        dwReplayCoreHandler.bufferEnd();
                    }
                    ELog.e(TAG, "render start");
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
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mTextureView.setVideoSize(videoWidth, videoHeight);
            }
        }
    };


    IMediaPlayer.OnErrorListener errorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
            mVideoProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    dismissProgress();
                }
            });
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
            long progress = player.getCurrentPosition();
            long progressTime = Math.round((double) progress / 1000) * 1000;
            if (allTime - progressTime < 3000) {
                DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
                if (dwReplayCoreHandler != null) {
                    dwReplayCoreHandler.onPlayComplete();
                }
            }
        }
    };
}
