package com.bokecc.livemodule.live.morefunction.rtc;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.utils.AppRTCAudioManager;
import com.bokecc.livemodule.view.BaseLinearLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.rtc.CCRTCRender;

import org.webrtc.SurfaceViewRenderer;

/**
 * CC 直播连麦视频展示控件
 */
public class RTCVideoLayout extends BaseLinearLayout {

    private int[] mVideoSizes = new int[2]; // 远程视频的宽高

    private WindowManager wm;

    SurfaceViewRenderer mLocalRender;

    CCRTCRender mRemoteRender;

    AppRTCAudioManager mAudioManager;

    public RTCVideoLayout(Context context) {
        super(context);
    }

    public RTCVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RTCVideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initViews() {
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(mContext).inflate(R.layout.live_portrait_rtc_video, this, true);
        mLocalRender = findViewById(R.id.svr_local_render);
        mRemoteRender = findViewById(R.id.svr_remote_render);

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.initRtc(mLocalRender, mRemoteRender);
        }
    }

    private void processRemoteVideoSize(String videoSize) {
        String[] sizes = videoSize.split("x");
        int width = Integer.parseInt(sizes[0]);
        int height = Integer.parseInt(sizes[1]);
        double ratio = (double) width / (double) height;
        // 对于分辨率为16：9的，更改默认分辨率为16：10
        if (ratio > 1.76 && ratio < 1.79) {
            mVideoSizes[0] = 1600;
            mVideoSizes[1] = 1000;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 连麦时切换全屏，自动适配大小
//        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
//        if (dwLiveCoreHandler != null && dwLiveCoreHandler.isRtcing() && needAdjust) {
////            mRemoteRender.setLayoutParams(getRemoteRenderSizeParams());
//        }
    }

    boolean needAdjust = false;

    public void enterSpeak(final boolean isVideoRtc, final boolean needAdjust, String videoSize) {
        this.needAdjust = needAdjust;
        processRemoteVideoSize(videoSize);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isVideoRtc) {
                    setVisibility(VISIBLE);
                    mLocalRender.setVisibility(View.INVISIBLE);
                    mRemoteRender.setVisibility(View.VISIBLE);
                    if (needAdjust) {
//                        mRemoteRender.setLayoutParams(getRemoteRenderSizeParams());
                    }
                    DWLive.getInstance().removeLocalRender();
                } else {
                    setVisibility(GONE);
                    mLocalRender.setVisibility(View.INVISIBLE);
                    mRemoteRender.setVisibility(View.VISIBLE);
                    if (needAdjust) {
//                        mRemoteRender.setLayoutParams(getRemoteRenderSizeParams());
                    }
                }
                // 由于rtc是走的通话音频，所以需要做处理
                mAudioManager = AppRTCAudioManager.create(mContext, null);
                mAudioManager.init();
            }
        });
    }

    public void disconnectSpeak() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
                if (mAudioManager != null) {
                    mAudioManager.close();
                }
            }
        });
    }

    public void speakError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
                toastOnUiThread(e.getLocalizedMessage());
                if (mAudioManager != null) {
                    mAudioManager.close();
                }
            }
        });
    }

    //***************************************** 工具方法 *****************************************
//
//    // 连麦远端视频组件等比缩放
//    private LinearLayout.LayoutParams getRemoteRenderSizeParams() {
//        int width;
//        int height;
//
//        if (isPortrait()) {
//            width = wm.getDefaultDisplay().getWidth();
//            height = wm.getDefaultDisplay().getHeight() / 3; //TODO 可以根据当前布局方式更改此参数
//        } else {
//            width = wm.getDefaultDisplay().getWidth();
//            height = wm.getDefaultDisplay().getHeight();
//        }
//
//        int vWidth = mVideoSizes[0];
//        int vHeight = mVideoSizes[1];
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
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
//        params.gravity = Gravity.CENTER;
//        return params;
//    }
}
