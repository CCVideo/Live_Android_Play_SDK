package com.bokecc.livemodule.replaymix.video;

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
import com.bokecc.livemodule.replaymix.DWReplayMixCoreHandler;
import com.bokecc.livemodule.replaymix.DWReplayMixVideoListener;

import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * CC 回放视频展示控件
 */
public class ReplayMixVideoView extends RelativeLayout implements DWReplayMixVideoListener {

    Context mContext;

    View mRootView;

    TextureView mTextureView;

    TextView mVideoNoplayTip;

    ProgressBar mVideoProgressBar;

    DWReplayPlayer player;

    Surface surface;
    private float currentSpeed = 1.0f;

    public ReplayMixVideoView(Context context) {
        super(context);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public ReplayMixVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    public ReplayMixVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        player.setOnInfoListener(infoListener);
        player.setOnBufferingUpdateListener(bufferingUpdateListener);

        DWReplayMixCoreHandler mixCoreHandler = DWReplayMixCoreHandler.getInstance();

        if (mixCoreHandler != null) {
            mixCoreHandler.setPlayer(player);
            mixCoreHandler.setDwReplayMixVideoListener(this);
        }
    }

    /**
     * 添加此字段的意义在于：部分手机HOME到桌面回来时不触发onSurfaceTextureAvailable，需要由onResume来触发一次调用逻辑。
     */
    boolean hasCallStartPlay = false;

    /**
     * 开始播放
     */
    public synchronized void start() {
        if (hasCallStartPlay) {
            return;
        }
        hasCallStartPlay = true;
        DWReplayMixCoreHandler dwReplayMixCoreHandler = DWReplayMixCoreHandler.getInstance();
        if (dwReplayMixCoreHandler != null) {
            dwReplayMixCoreHandler.onSurfaceAvailable(surface, true);
        }
    }

    /**
     * 开始播放（当前Surface可用）
     */
    private void onSurfaceAvailable(boolean needStartPlay) {
        DWReplayMixCoreHandler mixCoreHandler = DWReplayMixCoreHandler.getInstance();
        if (mixCoreHandler != null) {
            mixCoreHandler.onSurfaceAvailable(surface, needStartPlay);
        }
    }

    long currentPosition;

    /**
     * 停止播放
     */
    public void pause() {
        hasCallStartPlay = false;  // 准备正常播放了，将字段回归为false
        if (player != null) {
            player.pause();
            if (player.getCurrentPosition() != 0) {
                currentSpeed = player.getSpeed(0);
                currentPosition = player.getCurrentPosition();
            }
        }
        DWReplayMixCoreHandler mixCoreHandler = DWReplayMixCoreHandler.getInstance();
        if (mixCoreHandler != null) {
            mixCoreHandler.pause();
        }
    }

    public void destroy() {
        DWReplayMixCoreHandler mixCoreHandler = DWReplayMixCoreHandler.getInstance();
        if (mixCoreHandler != null) {
            mixCoreHandler.destroy();
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

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            // 使用新的surfaceTexture生成surface
            surface = new Surface(surfaceTexture);

            // 正在播放中或者暂停中，只需要将新的surface给player即可
            if (player.isPlaying() || (player.isPlayable())) {
                onSurfaceAvailable(false);
                // 尝试绘制之前的画面
                try {
                    if (tempBitmap != null && !tempBitmap.isRecycled() && surface != null && surface.isValid()) {
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
            } else {
                // 如果不是播放中或者暂停中，就触发开始播放的操作（此操作是从头开始播放）
                if (hasCallStartPlay) {
                    return;
                }
                onSurfaceAvailable(true);
                hasCallStartPlay = true;
            }
            if(player != null){
                player.updateSurface(surface);
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
                    mVideoProgressBar.setVisibility(VISIBLE);
                    player.start();
                    hasCallStartPlay = false;  // 准备正常播放了，将字段回归为false
                    if (currentPosition > 0) {
                        player.setSpeed(currentSpeed);
                        player.seekTo(currentPosition);
                    }
                    mVideoNoplayTip.setVisibility(GONE);

                    DWReplayMixCoreHandler mixCoreHandler = DWReplayMixCoreHandler.getInstance();
                    if (mixCoreHandler != null) {
                        mixCoreHandler.replayVideoPrepared();
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

    IMediaPlayer.OnBufferingUpdateListener bufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            DWReplayMixCoreHandler mixCoreHandler = DWReplayMixCoreHandler.getInstance();
            if (mixCoreHandler != null) {
                mixCoreHandler.updateBufferPercent(percent);
            }
        }
    };

    /**
     * 回调：要开始播放另一个回放视频
     */
    @Override
    public void onPlayOtherReplayVideo() {
        // 这里主要做的事情是清零记忆的播放位置，防止播放其他的回放视频时出现不该有的seek操作
        currentPosition = 0;
        currentSpeed = 1.0f;
    }
}
