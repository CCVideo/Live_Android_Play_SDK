package com.bokecc.livemodule.localplay.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 回放直播间文档展示控件
 */
public class LocalReplayDocComponent extends LinearLayout {
    private final int SCALE_CENTER_INSIDE = 0;
    private final int SCALE_FIT_XY = 1;
    private final int SCALE_CROP_CENTER = 2;

    private int mCurrentScaleType = SCALE_CENTER_INSIDE;
    private Context mContext;

    private DocView mDocView;

    public LocalReplayDocComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public LocalReplayDocComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        mDocView = new DocView(mContext);
        mDocView.setScrollable(false); // 为了保证悬浮窗能正常处理触摸事件，必须将文档WebView响应滑动禁用调，且小窗模式建议使用文档的适应窗口模式
        mDocView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mDocView);

        DWLocalReplayCoreHandler replayCoreHandler = DWLocalReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            replayCoreHandler.setDocView(mDocView);
        }
    }

    public void setScaleType(int type) {
        mCurrentScaleType = type;
        if (SCALE_CENTER_INSIDE == mCurrentScaleType) {
            DWLiveLocalReplay.getInstance().setDocScaleType(DocView.ScaleType.CENTER_INSIDE);
        } else if (SCALE_FIT_XY == mCurrentScaleType) {
            DWLiveLocalReplay.getInstance().setDocScaleType(DocView.ScaleType.FIT_XY);
        } else if (SCALE_CROP_CENTER == mCurrentScaleType) {
            DWLiveLocalReplay.getInstance().setDocScaleType(DocView.ScaleType.CROP_CENTER);
        }
    }


    // 设置文档区域是否可滑动
    public void setDocScrollable(boolean scrollable) {
        if (mDocView != null) {
            mDocView.setScrollable(scrollable);
        }
    }
}
