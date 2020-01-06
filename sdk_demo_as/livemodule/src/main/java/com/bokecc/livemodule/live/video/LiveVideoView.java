package com.bokecc.livemodule.live.video;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveVideoListener;
import com.bokecc.livemodule.view.ResizeTextureView;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLivePlayer;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.logging.ELog;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * CC 直播视频展示控件
 * 说明: 此处存在Surface动态初始化失败的问题，后续考虑怎么优化
 *
 */
public class LiveVideoView extends RelativeLayout implements DWLiveVideoListener {
    private final String TAG = LiveVideoView.class.getSimpleName();

    private Context mContext;

    private WindowManager wm;

    private View mRootView;

    /**
     * 视频显示容器
     */
    private ResizeTextureView mVideoContainer;

    /**
     * 视频加载进度
     */
    private ProgressBar mVideoProgressBar;

    /**
     * 直播播放器
     */
    private DWLivePlayer player;

    /**
     * 缓存切换视频文档时的图像防止黑屏
     */
    private Bitmap tempBitmap;

    /**
     * 直播视频通知接口
     */
    private OnPreparedCallback preparedCallback;

    /**
     * 添加此字段的意义在于：
     * 部分手机HOME到桌面回来时不触发onSurfaceTextureAvailable，需要由onResume来触发一次调用逻辑。
     * 此字段在调用开始播放的时候使用，后面无论播放是否开始都需要在合适的时机恢复为false.
     */
    boolean hasCallStartPlay = false;
    private View mVideoNoplayTip;

    /**
     * 直播视频通知接口
     */
    public interface OnPreparedCallback {
        /**
         * 视频播放器已经转备好了
         *
         * @param videoView
         */
        void onPrepared(LiveVideoView videoView);
    }

    public LiveVideoView(Context context) {
        super(context);
        initViews(context);
    }

    public LiveVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public LiveVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    public void initViews(Context context) {
        this.mContext = context;
        inflateViews();
        initPlayer();
    }

    /**
     * 初始化视图对象
     */
    private void inflateViews() {
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.live_video_view, null);
        mVideoContainer = mRootView.findViewById(R.id.live_video_container);
        mVideoProgressBar = mRootView.findViewById(R.id.video_progressBar);
        mVideoNoplayTip = mRootView.findViewById(R.id.tv_video_no_play_tip);
        mVideoNoplayTip.setVisibility(GONE);
        addView(mRootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
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

    /**
     * 获取播放器视频对象
     */
    public DWLivePlayer getPlayer() {
        return player;
    }

    /**
     * 设置视频播放器准备状态监听回调
     *
     * @param preparedCallback
     */
    public void setPreparedCallback(OnPreparedCallback preparedCallback) {
        this.preparedCallback = preparedCallback;
    }

    /**
     * 视频播放控件进入连麦模式
     *
     * @param isVideoRtc : 是否显示连麦
     */
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

    /**
     * 视频播放控件退出连麦模式
     */
    public void exitRtcMode() {
        try {
            DWLive.getInstance().restartVideo(mSurface);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DWLiveException e) {
            e.printStackTrace();
        }
        setVisibility(VISIBLE);
    }

    /**
     * 开始播放
     *
     */
    public synchronized void start() {
//        ELog.i(TAG,"---start--- hasCallStartPlay: "+hasCallStartPlay);
//        if (hasCallStartPlay || null == surface) {
//            return;
//        }
//        hasCallStartPlay = true;
        // 启动直播播放器
        DWLiveCoreHandler.getInstance().start(null);
        if (mVideoProgressBar != null) {
            mVideoProgressBar.setVisibility(VISIBLE);
        }
    }

    /**
     * 停止播放
     *
     */
    public void stop() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.stop();
        }
    }

    public void destroy() {
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

    /**
     * 文档视频切换时缓存切换前的画面
     *
     */
    public void cacheScreenBitmap() {
        tempBitmap = mVideoContainer.getBitmap();
    }

    /**
     * 恢复暂停时的图像
//     *
//     */
//    public void showPauseCover() {
//        if (tempBitmap != null
//                && !tempBitmap.isRecycled() && surface != null && surface.isValid()) {
//            try {
//                int width = mVideoContainer.getWidth();
//                int height = mVideoContainer.getHeight();
//                RectF rectF = new RectF(0, 0, width, height);
//                Canvas canvas = surface.lockCanvas(new Rect(0, 0, width, height));
//                if (canvas != null) {
//                    canvas.drawBitmap(tempBitmap, null, rectF, null);
//                    surface.unlockCanvasAndPost(canvas);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (null != tempBitmap && !tempBitmap.isRecycled()) {
//                    try {
//                        tempBitmap.recycle();
//                        tempBitmap = null;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i(TAG, "onSurfaceTextureAvailable:");
            if (mSurfaceTexture != null) {
                mVideoContainer.setSurfaceTexture(mSurfaceTexture);
            } else {
                mSurfaceTexture = surfaceTexture;
                mSurface = new Surface(surfaceTexture);
                player.setSurface(mSurface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    /**
     * 直播准备监听器
     */
    IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            post(new Runnable() {
                @Override
                public void run() {
                    // 准备正常播放了，将字段回归为false;
                    if (null != mSurface) {
                        player.setSurface(mSurface);
                    }
                    ELog.i("sdk_bokecc","onPrepared...");
                    mVideoProgressBar.setVisibility(VISIBLE);
                    // 通知直播视频已经准备就绪
                    if (null != preparedCallback) {
                        preparedCallback.onPrepared(LiveVideoView.this);
                    }
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

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mVideoContainer.setLayoutParams(getVideoSizeParams());
//    }

    IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mVideoContainer.setVideoSize(videoWidth, videoHeight);
            }
        }
    };


    // 判断当前屏幕朝向是否为竖屏
    private boolean isPortrait() {
        int mOrientation = mContext.getResources().getConfiguration().orientation;
        return mOrientation != Configuration.ORIENTATION_LANDSCAPE;
    }

//    /**
//     * 视频等比缩放
//     *
//     */
//    private RelativeLayout.LayoutParams getVideoSizeParams() {
//
//        int width = wm.getDefaultDisplay().getWidth();
//        int height;
//        if (isPortrait()) {
//            height = wm.getDefaultDisplay().getHeight() / 3;  //TODO 可以根据当前布局方式更改此参数
//        } else {
//            height = wm.getDefaultDisplay().getHeight();
//        }
//
//
//        int vWidth = player.getVideoWidth();
//        int vHeight = player.getVideoHeight();
//
//        if (vWidth == 0) {
//            vWidth = 600;
//        }
//        if (vHeight == 0) {
//            vHeight = 400;
//        }
//
//        if (vWidth > width || vHeight > height) {
//            float wRatio = (float) vWidth / (float) width;
//            float hRatio = (float) vHeight / (float) height;
//            float ratio = Math.max(wRatio, hRatio);
//
//            width = (int) Math.ceil((float) vWidth / ratio);
//            height = (int) Math.ceil((float) vHeight / ratio);
//        } else {
//            float wRatio = (float) width / (float) vWidth;
//            float hRatio = (float) height / (float) vHeight;
//            float ratio = Math.min(wRatio, hRatio);
//
//            width = (int) Math.ceil((float) vWidth * ratio);
//            height = (int) Math.ceil((float) vHeight * ratio);
//        }
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
//        params.addRule(RelativeLayout.CENTER_IN_PARENT);
//        return params;
//    }

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
            }
        });
    }

    /**
     * 播放状态
     *
     * @param status : 包括PLAYING, PREPARING共2种状态
     */
    @Override
    public void onLiveStatus(final DWLive.PlayStatus status) {
        mRootView.post(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case PLAYING:
                        // 直播正在播放
                        mVideoProgressBar.setVisibility(VISIBLE);
                        break;
                    case PREPARING:
                        // 直播未开始
                        mVideoProgressBar.setVisibility(GONE);
                        // 如果判断当前直播未开始，也将字段回归为false;
                        hasCallStartPlay = false;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 禁播
     *
     * @param reason : 禁播原因
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
            }
        });
    }

    /**
     * 解禁
     */
    @Override
    public void onUnbanStream() {
        if (mSurface != null) {
            DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
            if (dwLiveCoreHandler != null) {
                dwLiveCoreHandler.start(mSurface);
            }
        }
    }
}
